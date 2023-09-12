#include <stdio.h>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/un.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <pthread.h>
#include "client_struct/client_s.h"
#include "list/list_s.h"
#include "chat_struct/chat_s.h"
#include <signal.h>
#include <arpa/inet.h>
#define MAX_LEN 200

List *lista;
int socketfd;

void handlerExit(int num);
void *fun_start_accept(void *socket);
void afterChatting(Client *client);
void *chatWithClient(void *chat);
void *chatting(void *chat);
void *fun_pairer(void *arg);

int main()
{

    struct sockaddr_in address;
    bzero((void *)&address, sizeof(address));
    address.sin_family = AF_INET;
    address.sin_port = htons(10000);
    address.sin_addr.s_addr = htonl(INADDR_ANY);
    lista = createList();

    signal(SIGINT, handlerExit);

    if ((socketfd = socket(AF_INET, SOCK_STREAM, 0)) == -1)
    {
        perror("Errore creazione socket");
        return -1;
    }

    printf("pre-bind\n");

    if (setsockopt(socketfd, SOL_SOCKET, SO_REUSEADDR, &(int){1}, sizeof(int)) < 0)
        perror("setsockopt(SO_REUSEADDR) failed");
    if (setsockopt(socketfd, SOL_SOCKET, SO_REUSEPORT, &(int){1}, sizeof(int)) < 0)
        perror("setsockopt(SO_REUSEPORT) failed");

    if ((bind(socketfd, (struct sockaddr *)&address, sizeof(address))) == -1)
    {
        perror("Errore nel binding");
        return -2;
    }

    printf("bind\n");

    if ((listen(socketfd, 10)) == -1)
    {
        perror("Errore nel listen");
        return -3;
    }

    printf("listen\n");
    printf("Server...\n");

    pthread_t tid_handle_client;
    pthread_create(&tid_handle_client, NULL, fun_start_accept, (void *)&socketfd);
    pthread_t tid_pair_clients;
    pthread_create(&tid_pair_clients, NULL, fun_pairer, NULL);

    pthread_join(tid_handle_client, NULL);
    pthread_join(tid_pair_clients, NULL);

    return 0;
}

void handlerExit(int num)
{
    close(socketfd);
    free(lista);
    exit(0);
}

void subString(char buf[42], char nick[21], char last[21])
{
    int i = 0, j = 0;

    while (buf[i] != '@')
    {
        nick[i] = buf[i];
        i += 1;
    }

    nick[i] = ' ';
    nick[i+1] = '\0';
    i += 1;

    while (buf[i] != '\n')
    {
        last[j] = buf[i];
        i += 1;
        j += 1;
    }
    
    last[j] = ' ';
    last[j+1] = '\0';

}

void *addClientToList(void *fd)
{
    char buf[42], nickname[21], last[21];
    int n;
    bzero(buf, 42);
    pthread_t stateUpdate_tid;

    n = read(*((int *)fd), buf, 42);
    if (n <= 0)
        perror("Errore lettura nickname\n");
    
    subString(buf, nickname, last);

    printf("\nDopo la divisione ho NICK: %s ,LAST: %s", nickname, last);    

    // buf[strcspn(buf, "\n")] = 0;
    Client *client = createClient(*((int *)fd), nickname, last);
    printf("\nIl client ha NICK: %s ,LAST: %s", client->nickname, client->last_client);
    printf("ACCETTATO CLIENT. . .\n");
    printf("Il nome del client e' %s e il last è %s\n", client->nickname, client->last_client);

    printf("aggiungo il client %d alla lista\n", *((int *)fd));
    addClient(lista, client);
    printf("client %d aggiunto\n", *((int *)fd));
}

void *fun_start_accept(void *socket)
{
    struct sockaddr_in address_client;
    socklen_t client_len;
    int client_sock;
    Client *client;
    pthread_t addClientToList_tid;

    while (1)
    {
        printf("Sto per accettare\n");
        client_sock = accept(*((int *)socket), (struct sockaddr *)&address_client, &client_len);
        printf("Ho accettato\n");

        if (client_sock == -1)
        {
            perror("Errore accept\n");
        }

        pthread_create(&addClientToList_tid, NULL, addClientToList, (void *)&client_sock);
    }
}



void *chatWithClient(void *chat)
{
    char buf1[MAX_LEN];
    int n = 1, m = 1;
    Chat *tmp = (Chat *)chat;

    bzero(buf1, MAX_LEN);

    while ((tmp->client1->state == CHATTING) && (n = read((tmp->client1->fd), buf1, MAX_LEN)) > 0)
    {
        if (strncmp(buf1, "/END", 4) == 0)
        {
            printf("Sono client %s e mando end\n", tmp->client1->nickname);
            if (tmp->client2->state == CHATTING)
                write(tmp->client2->fd, "/END", sizeof("/END"));
            tmp->client1->state = AVAILABLE;
            bzero(buf1, MAX_LEN);
            free(tmp->client1);
            return 0;
        }
        else if ((m = write(tmp->client2->fd, buf1, n)) == 0)
        {
            perror("Errore scrittura client1\n");
        }
        write(STDOUT_FILENO, buf1, n);
        bzero(buf1, MAX_LEN);
    }
}

void *chatting(void *chat)
{
    printf("thread creato\n");
    pid_t child;
    Chat *tmpChat = (Chat *)chat;
    Chat *tmp2 = (Chat*) malloc(sizeof(Chat));
    tmp2->client1 = tmpChat->client2;
    tmp2->client2 = tmpChat->client1;
    pthread_t t1, t2;

    pthread_create(&t1, NULL, chatWithClient, chat);
    pthread_create(&t2, NULL, chatWithClient, (void *)tmp2);   
    
}

void *fun_pairer(void *arg)
{
    Chat *newChat = NULL;
    pthread_t tid_chat;
    pthread_t tid_timer;
    pid_t pid_afterChat;
    while (1)
    {
        // In questa funzione accoppio i client tra di loro
        newChat = chatPairing(lista);
        printf("Creo thread chatting\n");
        pthread_create(&tid_chat, NULL, chatting, (void *)newChat);
    }
}
