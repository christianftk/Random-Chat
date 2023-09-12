#include "client_s.h"

Client *createClient(int fd, char *nickname, char* last)
{
    Client *client = (Client *)malloc(sizeof(Client));
    client->fd = fd;
    client->state = AVAILABLE;
    bzero(client->nickname, 21);
    bzero(client->last_client, 21);
    strcpy(client->nickname, nickname);
    strcpy(client->last_client, last);
    return client;
}

