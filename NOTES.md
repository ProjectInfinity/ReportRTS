ReportRTS v1.2.3 Changelog
====================

1. Further rename "Requests" to "Tickets", **events have been refactored to reflect this change.**
2. Added line breaks to fancy tickets that will make it easier to read.
3. Added a "by <username>" to claimed tickets. Previously it only said it was claimed for X amount of time.
4. Bug fixes!
    * Added missing comment on closed tickets.
    * Fix numbers from appearing in comments when closing a ticket.
    * Fix claiming and unclaiming tickets using console.
5. The **_/reportrts stats_** command has been rewritten. It now works as a "top 10 staff", listing them by most resolved tickets.
6. Some permission nodes have changed.
    * **reportrts.mod** is now **reportrts.staff**, reportrts.mod has been added as a pointer to reportrts.staff in plugin.yml.
7. Time in /ticket read is now displayed in RELATIVE time. Closed or held tickets will still show actual date.
8. Performance improvements.
    * Globally store Console user ID to avoid extra queries.
9. Completely rewritten data-provider system! Massive improvements.