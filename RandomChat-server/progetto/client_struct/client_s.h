#ifndef STRUCT_H
#define STRUCT_H

#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>

#define AVAILABLE 1
#define CHATTING 2

typedef struct Client
{
    int fd;
    char nickname[21];
    char last_client[21];
    int state;
} Client;

Client *createClient(int fd, char *nickname, char* last);

#endif
