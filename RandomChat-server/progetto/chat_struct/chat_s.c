#include "chat_s.h"
#include <string.h>
#include <errno.h>
#include <signal.h>

Chat *chatPairing(List *list)
{
    Node *current = NULL;
    Chat *chat = (Chat *)malloc(sizeof(Chat));
    Node *nodo1, *nodo2;
    
    while (1)
    {
        sleep(1);
        if (list->length > 0)
        {
            current = list->head;
            while (current != NULL)
            {
                sleep(1);
                
                printf("il client è: %s \n", current->client->nickname);
                if (current->client->state == AVAILABLE)
                {
                    if (chat->client1 == NULL)
                    {
                        printf("setto client1\n");                        
                        chat->client1 = current->client;
                        nodo1 = current;
                        removeClient(list, chat->client1);
                    }
                    else if (chat->client2 == NULL)
                    {
                                                
                        if (chat->client1 != current->client && strcmp(chat->client1->last_client, current->client->nickname) != 0)
                        {
                            printf("\nConfrontando %s, e %s, risultano essere diversi \n", chat->client1->last_client, current->client->nickname);
                            printf("setto client2\n");
                            chat->client2 = current->client;
                            nodo2 = current;
                            removeClient(list, chat->client2);
                        }
                    }
                    // Verificare se i 2 client sono ancora disponibili
                    if (chat->client1 != NULL && chat->client2 != NULL)
                    {
                        // Modifico i client prima di avviare la comunicazione
                        chat->client1->state = CHATTING;
                        strcpy(chat->client1->last_client, chat->client2->nickname);
                        chat->client2->state = CHATTING;
                        strcpy(chat->client2->last_client, chat->client1->nickname);
                        write(chat->client2->fd, chat->client1->nickname, strlen(chat->client1->nickname));
                        write(chat->client1->fd, chat->client2->nickname, strlen(chat->client2->nickname));
                        write(STDOUT_FILENO, chat->client1->nickname, strlen(chat->client1->nickname));
                        write(STDOUT_FILENO, chat->client2->nickname, strlen(chat->client2->nickname));                        
                        free(nodo1);
                        free(nodo2);
                        return chat;
                    }
                }
                printf("prendo successivo\n");
                current = current->next;
            }
            // Fine della lista, bisogna ricominciare dall'inizio
            current = list->head;
        }
    }
}
