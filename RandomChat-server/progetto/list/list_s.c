#include "list_s.h"

Node *createNode(Client *client)
{
    Node *node;
    node = (Node *)malloc(sizeof(Node));
    pthread_mutex_init(&(node->nodeMutex), NULL);
    node->client = client;
    node->next = NULL;
    return node;
}

List *createList()
{
    List *list;
    list = (List *)malloc(sizeof(List));
    pthread_mutex_init(&(list->listMutex), NULL);
    list->head = NULL;
    list->tail = NULL;
    list->length = 0;
    return list;
}

void addClient(List *lista, Client *client)
{
    pthread_mutex_lock(&(lista->listMutex));
    Node *nodo = createNode(client);
    if (lista->length == 0)
    {
        lista->head = nodo;
        lista->tail = nodo;
    }
    else
    {
        lista->tail->next = nodo;
        lista->tail = nodo;
    }
    lista->length++;
    pthread_mutex_unlock(&(lista->listMutex));
}

Client *head(List *lista)
{
    if (lista->head == NULL)
        return NULL;
    return lista->head->client;
}


int isEmpty(List *lista)
{
    if (lista != NULL)
    {
        if (lista->head != NULL)
        {
            return 1;
        }
    }
    return 0;
}

void removeClient(List *lista, Client *client)
{
    Node *current, *prev;
    current = lista->head;
    prev = NULL;

    printf("Sto rimuovendo il client: %s e la lungezza è: %d\n", client->nickname, lista->length);
    pthread_mutex_lock(&(lista->listMutex));
    if (isEmpty(lista) == 1)
    {
        if(lista->length > 1)
        {
            while (current->client != client)
            {
                prev = current;
                current = current->next;
            }


            if (prev == NULL)
                lista->head = current->next;
            else if (current->next == NULL)
            {
                prev->next = NULL;
                lista->tail = prev;
                
            }
            else
            {
                prev->next = current->next;
            }
        }
        else
        {
            lista->head = NULL;
            lista->tail = NULL;
        }



        lista->length--; 
        
        
        current = lista->head;
        while(current != NULL){
            printf("\n\nnickname: %s\n", current->client->nickname);
            current = current->next;
        }
    }    
    pthread_mutex_unlock(&(lista->listMutex));
}
