package com.example.code

/**
 * A notification sent to one user: a message plus what it is about.
 *
 * @property message who sent it, who gets it, the text and the time
 * @property type what the notification is about
 */
class Notification(
    var message: Message = Message(),
    var type: NotificationType = NotificationType.OWNER_ANNOUNCEMENT
)
