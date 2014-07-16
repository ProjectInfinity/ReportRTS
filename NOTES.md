Notes about ReportRTS version 1.2.2
====================

1. New command system.
    * Allows for legacy-like command system by setting the legacy option to true in the configuration.
        * Legacy commands do not work from console.
        * Tab completion does not work with legacy commands.
    * Most commands work from console.
2. Requests have been renamed to tickets.
    * Please regenerate your messages.yml file to reflect these new changes.
3. Some messages instructing users to type a specific command is now dynamic to reflect the new dynamic command system.
    * Please regenerate your messages.yml file to reflect these new changes.
4. Several bugs have been fixed.
    * Fixed the ancient "claimed by null" issue when assigning tickets or claiming tickets.
    * Fixed incorrectly passed UUID to BungeeCord when it wanted a username instead.
    * Fixed broken pagination when attempting to view held tickets.
    * Fixed UUID being passed instead of username when a player attempted to open a ticket by placing a sign.
    * Fixed a NullPointerException that would occur if a player attempted to teleport to a ticket that referenced world that did no longer exist on the server.
    * Fixed an issue in isNumber() that caused it to return true when a negative number was passed.
    * Fixed an issue resulting in a NumberFormatException should the user provide a number larger than the max size of an Integer.