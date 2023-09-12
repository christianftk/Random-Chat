#ifndef LIST_H
#define LIST_H

#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include "../client_struct/client_s.h"

typedef struct Node
{
    Client *client;
    struct Node* next;
    pthread_mutex_t nodeMutex;
} Node;

Node *createNode(Client *client);

typedef struct List
{
    Node *head;
    Node *tail;
    int length;
    pthread_mutex_t listMutex;
} List;

List *createList();
void addClient(List *lista, Client *client);
Client *head(List *lista);        // viene usato per prendere il secondo client senza toglierlo dalla listae viene confrontato con "last_client"
Client *headNRemove(List *lista); // usato per prendere il primo client
int isEmpty(List *lista);         // 0 vuota 1 piena
void removeClient(List* lista, Client* client);

#endif
