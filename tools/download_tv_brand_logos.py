#!/usr/bin/env python3
"""Download TV brand logos and generate the Android logo resolver.

The app must work offline, so this script downloads logo images during
development and stores only local drawable resources in the APK.
"""

from __future__ import annotations

import json
import io
import os
import re
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

try:
    from PIL import Image
except ImportError:  # The generated assets remain usable; trimming is best-effort.
    Image = None


ROOT = Path(__file__).resolve().parents[1]
BRAND_ROOT = ROOT / "app/src/main/assets/irdb/tv"
DRAWABLE_DIR = ROOT / "app/src/main/res/drawable-nodpi"
MANIFEST_PATH = ROOT / "app/src/main/assets/brand_logos/brand_logo_sources.json"
RESOLVER_PATH = ROOT / "app/src/main/java/com/example/lcb/app/remote/ui/BrandLogoResolver.kt"

UPLEAD_LOGO_URL = "https://logo.uplead.com/{domain}"
GOOGLE_FAVICON_URL = "https://www.google.com/s2/favicons?domain={domain}&sz=128"
USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15"
BRANDFETCH_CLIENT_ID = os.environ.get("BRANDFETCH_CLIENT_ID", "1id2KPHxY5IIyj3mB1s").strip()
BRANDFETCH_LOGO_API_URL = (
    "https://cdn.brandfetch.io/domain/{domain}/w/{width}/h/{height}/"
    "theme/{theme}/fallback/404/type/{logo_type}.{format}?c={client_id}"
)

# These responses were visually inspected and did not match the TV brand, or
# the brand name is too generic to map safely without manual art direction.
EXCLUDED_BRAND_KEYS = {
    "dyon",
    "united",
    "viano",
    "wbox",
}

# Source order is configurable per brand because short/regional TV brands often
# have a correct domain but a low-contrast or generic company-logo CDN result.
DEFAULT_SOURCE_ORDER = ("brandfetch", "uplead", "google_favicon")
SOURCE_ORDER_BY_BRAND = {
    "apex": ("google_favicon", "uplead", "brandfetch"),
    "brandt": ("google_favicon", "brandfetch", "uplead"),
    "devant": ("uplead", "google_favicon", "brandfetch"),
    "ffalcon": ("google_favicon", "brandfetch", "uplead"),
    "philips": ("google_favicon", "brandfetch", "uplead"),
    "rca": ("google_favicon", "brandfetch", "uplead"),
    "vitec": ("google_favicon", "uplead", "brandfetch"),
}

# Domains are intentionally explicit for accuracy. Autocomplete APIs are fast,
# but they often return unrelated companies for short or regional TV brands.
BRAND_DOMAINS = {
    "akai": "akai.com",
    "apex": "apexdigital.com",
    "awa": "awa.com.au",
    "amazon": "amazon.com",
    "amazonbasics": "amazon.com",
    "android": "android.com",
    "bauhn": "bauhn.com.au",
    "bgh": "bgh.com.ar",
    "blaupunkt": "blaupunkt.com",
    "blitzwolf": "blitzwolf.com",
    "bose": "bose.com",
    "boulanger": "boulanger.com",
    "brandt": "brandt.fr",
    "bbk": "bbk.ru",
    "bush": "bush-support.com",
    "cadillac": "cadillac.com",
    "continentaledison": "continentaledison.fr",
    "daewoo": "daewooelectronics.com",
    "denver": "denver.eu",
    "devant": "devanttv.com",
    "dual": "dual.de",
    "dyon": "dyon.eu",
    "dynex": "dynexproducts.com",
    "element": "elementelectronics.com",
    "elements": "elementelectronics.com",
    "emerson": "emerson.com",
    "enseo": "enseo.com",
    "finlux": "finlux.com",
    "fetch": "fetchtv.com.au",
    "ffalcon": "ffalcon.com.au",
    "funai": "funaiamerica.com",
    "furrion": "furrion.com",
    "gpx": "gpx.com",
    "gigabyte": "gigabyte.com",
    "gogen": "gogen.cz",
    "grundig": "grundig.com",
    "guesttek": "guesttek.com",
    "haier": "haier.com",
    "hisense": "hisense.com",
    "hitachi": "hitachi.com",
    "insignia": "insigniaproducts.com",
    "jvc": "jvc.com",
    "kogan": "kogan.com",
    "lg": "lg.com",
    "loewe": "loewe.tv",
    "magnavox": "magnavox.com",
    "manta": "manta.com.pl",
    "medion": "medion.com",
    "mivar": "mivar.it",
    "nec": "nec.com",
    "nevir": "nevir.es",
    "onn": "onntvsupport.com",
    "panasonic": "panasonic.com",
    "pdi": "pdiarm.com",
    "philips": "philips.com",
    "pioneer": "pioneerelectronics.com",
    "prismplus": "prismplus.sg",
    "rca": "rca.com",
    "roku": "roku.com",
    "samsung": "samsung.com",
    "sanyo": "sanyo-av.com",
    "sceptre": "sceptre.com",
    "seiki": "seiki.com",
    "sencor": "sencor.com",
    "sharp": "sharpconsumer.com",
    "skyvue": "skyvue.com",
    "soniq": "soniq.com",
    "sony": "sony.com",
    "strong": "strong.tv",
    "sunbrite": "sunbritetv.com",
    "supra": "supra.ru",
    "sweex": "sweex.com",
    "tcl": "tcl.com",
    "telecontrole": "telecontrole.com.br",
    "telefunken": "telefunken.com",
    "telekom": "telekom.de",
    "thomson": "mythomson.com",
    "toshiba": "toshiba.com",
    "technika": "technika.com.au",
    "united": "united-electronics.gr",
    "viano": "viano.com.au",
    "viewsonic": "viewsonic.com",
    "vizio": "vizio.com",
    "vitec": "vitec.com",
    "wbox": "wboxtech.com",
    "westinghouse": "westinghouseelectronics.com",
    "xiaomi": "mi.com",
    "zenith": "zenith.com",
}


def brand_key(value: str) -> str:
    return re.sub(r"[^a-z0-9]", "", value.lower())


def resource_slug(value: str) -> str:
    slug = re.sub(r"[^a-z0-9]+", "_", value.lower()).strip("_")
    return slug or "brand"


def request_bytes(url: str) -> bytes | None:
    request = urllib.request.Request(
        url,
        headers={
            "Accept": "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8",
            "Referer": "https://brandfetch.com/developers/logo-api",
            "Sec-Fetch-Dest": "image",
            "User-Agent": USER_AGENT,
        },
    )
    try:
        with urllib.request.urlopen(request, timeout=18) as response:
            data = response.read()
            return data if is_supported_image(data) else None
    except (urllib.error.URLError, TimeoutError):
        return None


def is_supported_image(data: bytes) -> bool:
    return (
        data.startswith(b"\x89PNG\r\n\x1a\n")
        or data.startswith(b"\xff\xd8\xff")
        or data.startswith(b"RIFF") and data[8:12] == b"WEBP"
    )


def normalize_logo_bytes(data: bytes) -> bytes:
    if Image is None:
        return data

    try:
        image = Image.open(io.BytesIO(data)).convert("RGBA")
    except Exception:
        return data

    # Many logo CDNs return transparent or white-padded art. Cropping this
    # whitespace keeps the packaged logo visually balanced in compact list rows.
    alpha_box = image.getbbox()
    if alpha_box:
        image = image.crop(alpha_box)

    pixels = image.load()
    width, height = image.size
    left, top, right, bottom = width, height, 0, 0
    found_content = False
    for y in range(height):
        for x in range(width):
            red, green, blue, alpha = pixels[x, y]
            is_visible = alpha > 8
            is_near_white = red > 246 and green > 246 and blue > 246
            if is_visible and not is_near_white:
                left = min(left, x)
                top = min(top, y)
                right = max(right, x + 1)
                bottom = max(bottom, y + 1)
                found_content = True

    if found_content:
        image = image.crop((left, top, right, bottom))

    output = io.BytesIO()
    image.save(output, format="PNG")
    return output.getvalue()


def fallback_logo_sources(domain: str) -> dict[str, str]:
    return {
        "uplead": UPLEAD_LOGO_URL.format(domain=domain),
        "google_favicon": GOOGLE_FAVICON_URL.format(domain=domain),
    }


def brandfetch_logo_sources(domain: str) -> list[tuple[str, str]]:
    if not BRANDFETCH_CLIENT_ID:
        return []
    candidates = [
        ("brandfetch_logo_dark_png", "logo", "dark", 512, 256, "png"),
        ("brandfetch_logo_light_png", "logo", "light", 512, 256, "png"),
        ("brandfetch_symbol_dark_png", "symbol", "dark", 256, 256, "png"),
        ("brandfetch_icon_png", "icon", "dark", 256, 256, "png"),
    ]
    return [
        (
            source,
            BRANDFETCH_LOGO_API_URL.format(
                client_id=BRANDFETCH_CLIENT_ID,
                domain=domain,
                format=image_format,
                height=height,
                logo_type=logo_type,
                theme=theme,
                width=width,
            ),
        )
        for source, logo_type, theme, width, height, image_format in candidates
    ]


def logo_source_candidates(domain: str, brand_key_value: str) -> list[tuple[str, str]]:
    ordered_sources: list[tuple[str, str]] = []
    fallbacks = fallback_logo_sources(domain)
    source_order = SOURCE_ORDER_BY_BRAND.get(brand_key_value, DEFAULT_SOURCE_ORDER)

    for source_group in source_order:
        if source_group == "brandfetch":
            ordered_sources.extend(brandfetch_logo_sources(domain))
        elif source_group in fallbacks:
            ordered_sources.append((source_group, fallbacks[source_group]))

    return ordered_sources


def download_logo(domain: str, brand_key_value: str) -> tuple[bytes, str] | tuple[None, None]:
    for source, url in logo_source_candidates(domain, brand_key_value):
        data = request_bytes(url)
        if data:
            return normalize_logo_bytes(data), source
    return None, None


def write_resolver(downloaded: list[dict[str, str]]) -> None:
    entries = []
    for item in sorted(downloaded, key=lambda row: row["brand"].lower()):
        key = brand_key(item["brand"])
        res_name = Path(item["file"]).stem
        entries.append(f'        "{key}" to R.drawable.{res_name},')

    mapping = "\n".join(entries)
    content = f"""package com.example.lcb.app.remote.ui

import com.example.lcb.app.R

/**
 * Maps packaged TV brand logo assets to IRDB brand folder names.
 * Regenerate this file with tools/download_tv_brand_logos.py when brand assets change.
 */
object BrandLogoResolver {{
    private val logos = mapOf(
{mapping}
    )

    fun logoForName(name: String): Int? = logos[name.brandLogoKey()]
}}

fun String.brandLogoKey(): String = lowercase().filter {{ it.isLetterOrDigit() }}
"""
    RESOLVER_PATH.write_text(content, encoding="utf-8")


def main() -> int:
    brands = sorted(
        path.name for path in BRAND_ROOT.iterdir()
        if path.is_dir()
        and brand_key(path.name) in BRAND_DOMAINS
        and brand_key(path.name) not in EXCLUDED_BRAND_KEYS
    )
    DRAWABLE_DIR.mkdir(parents=True, exist_ok=True)
    MANIFEST_PATH.parent.mkdir(parents=True, exist_ok=True)

    downloaded: list[dict[str, str]] = []
    for brand in brands:
        key = brand_key(brand)
        domain = BRAND_DOMAINS[key]
        filename = f"brand_logo_{resource_slug(brand)}.png"
        output = DRAWABLE_DIR / filename
        data, source = download_logo(domain, key)
        if not data:
            print(f"miss {brand} <- {domain}")
            continue
        output.write_bytes(data)
        downloaded.append({
            "brand": brand,
            "domain": domain,
            "file": filename,
            "source": source or "unknown",
        })
        print(f"ok   {brand} <- {domain} ({source})")
        time.sleep(0.05)

    # Remove stale generated logo files only after this run has produced the new set.
    active_files = {item["file"] for item in downloaded}
    for stale_logo in DRAWABLE_DIR.glob("brand_logo_*.*"):
        if stale_logo.name not in active_files:
            stale_logo.unlink()

    MANIFEST_PATH.write_text(
        json.dumps(downloaded, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    write_resolver(downloaded)
    print(f"downloaded {len(downloaded)} logos")
    return 0


if __name__ == "__main__":
    sys.exit(main())
