#ifndef CHAT_S_H
#define CHAT_S_H

#include "../client_struct/client_s.h"
#include "../list/list_s.h"

#define BOOKS 0
#define MUSIC 1
#define GAMES 2
#define SPORT 3
#define FOOD 4

#define MAXTIMER 60

typedef struct Chat
{
    Client *client1;
    Client *client2;
    int timer;
    int room_topic;
} Chat;

Chat* chatPairing(List *lista); // TODO crea una chat con due client all'interno
void chatEnd(Chat *chat);      // TODO una volta finita la chat, setta i client in idle

#endif
