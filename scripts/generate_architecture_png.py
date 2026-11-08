#!/usr/bin/env python3
"""Draw a crisp architecture PNG titled Shopito — Architecture (no raster text ambiguity)."""
from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs" / "screenshots" / "01-architecture-shopito.png"


def _font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    candidates = (
        "C:/Windows/Fonts/segoeuib.ttf" if bold else "C:/Windows/Fonts/segoeui.ttf",
        "C:/Windows/Fonts/arialbd.ttf" if bold else "C:/Windows/Fonts/arial.ttf",
    )
    for path in candidates:
        if Path(path).is_file():
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def main() -> None:
    w, h = 960, 420
    bg = "#f8fafc"
    teal = "#0f766e"
    border = "#99f6e4"
    arrow = "#14b8a6"

    img = Image.new("RGB", (w, h), bg)
    draw = ImageDraw.Draw(img)
    title_font = _font(28, bold=True)
    label_font = _font(20, bold=True)
    sub_font = _font(15)
    pill_font = _font(13, bold=True)

    draw.text((w // 2, 36), "Shopito — Architecture", fill=teal, font=title_font, anchor="mm")

    boxes = [
        (60, 110, 300, 350, "Frontend", "React + Vite", "Vercel"),
        (360, 110, 600, 350, "Backend API", "Spring Boot 3 + JWT", "Railway"),
        (660, 110, 900, 350, "Database", "PostgreSQL", "Railway"),
    ]
    for x1, y1, x2, y2, title, tech, host in boxes:
        draw.rounded_rectangle((x1, y1, x2, y2), radius=18, fill="#ffffff", outline=border, width=2)
        cx = (x1 + x2) // 2
        draw.text((cx, y1 + 42), title, fill="#134e4a", font=label_font, anchor="mm")
        draw.text((cx, y1 + 72), tech, fill="#334155", font=sub_font, anchor="mm")
        pill = (cx - 75, y2 - 55, cx + 75, y2 - 21)
        draw.rounded_rectangle(pill, radius=17, fill="#ecfdf5", outline="#5eead4", width=1)
        draw.text((cx, y2 - 38), host, fill=teal, font=pill_font, anchor="mm")

    draw.line((302, 230, 358, 230), fill=arrow, width=3)
    draw.polygon([(358, 230), (348, 225), (348, 235)], fill=arrow)
    draw.text((330, 210), "HTTPS + JWT", fill="#334155", font=sub_font, anchor="mm")

    draw.line((602, 230, 658, 230), fill=arrow, width=3)
    draw.polygon([(658, 230), (648, 225), (648, 235)], fill=arrow)
    draw.text((630, 210), "JDBC", fill="#334155", font=sub_font, anchor="mm")

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT, format="PNG", optimize=True)
    print(f"Wrote {OUT} ({OUT.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
