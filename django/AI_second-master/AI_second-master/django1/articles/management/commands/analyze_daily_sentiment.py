from django.core.management.base import BaseCommand
from typing import Optional
from articles.services.daily_sentiment import analyze_and_write_to_chat_room

class Command(BaseCommand):
    help = "Daily: analyze last ~100 msgs per room and write 1/1 or 0/0 to chat_room."

    def add_arguments(self, parser):
        parser.add_argument("--rooms", nargs="*", type=int, help="Specific room_ids (omit = all)")

    def handle(self, *args, **opts):
        room_ids = opts.get("rooms")
        res = analyze_and_write_to_chat_room(room_ids)
        if not res:
            self.stdout.write(self.style.WARNING("No rooms processed (no messages or empty set)."))
            return
        for rid, info in res.items():
            self.stdout.write(self.style.SUCCESS(
                f"[room {rid}] msgs={info['count']} pos_avg={info['pos_avg']:.3f} flag={info['flag']}"
            ))
