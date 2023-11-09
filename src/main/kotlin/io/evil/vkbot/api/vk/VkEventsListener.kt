package io.evil.vkbot.api.vk

import com.vk.api.sdk.actions.LongPoll
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import org.springframework.stereotype.Component
import kotlin.concurrent.thread

@Component
class VkEventsListener(
    private val vkClient: VkApiClient,
    private val vkActor: GroupActor,
    private val bus: VkEventBus
) {
    fun listen() {
        thread(isDaemon = true) {
            val serverResponse = vkClient.groups().getLongPollServer(vkActor).execute()
            val longPoll = LongPoll(vkClient)
            while (true) {
                longPoll
                    .getEvents(
                        serverResponse.server,
                        serverResponse.key,
                        serverResponse.ts
                    )
                    .waitTime(60)
                    .execute()
                    .run {
                        updates.forEach { bus.fire(it) }
                        serverResponse.ts = ts
                    }
            }
        }
    }
}
