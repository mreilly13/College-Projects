#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/wait.h>
#include <sys/types.h>

// accept up to 16 command-var arguments
#define MAXARG 16

// allow up to 64 environment variables
#define MAXENV 64

// keep the last 500 commands in hist
#define HISTSIZE 500

// accept up to 1024 bytes in one command
#define MAXLINE 1024

// function to parse a command - provided
static char **parseCmd(char cmdLine[]) {
  char **cmdArg, *ptr;
  int i;
  cmdArg = (char **) malloc(sizeof(char *) * (MAXARG + 1));
  if (cmdArg == NULL) {
    perror("parseCmd - cmdArg is NULL");
    exit(1);
  }
  for (i = 0; i <= MAXARG; i++) {
    cmdArg[i] = NULL;
  }
  i = 0;
  ptr = strsep(&cmdLine, " ");
  while (ptr != NULL) {
    cmdArg[i] = (char *) malloc(sizeof(char) * (strlen(ptr) + 1));
    if (cmdArg[i] == NULL) {
      perror("parseCmd - cmdArg[i] is NULL");
      exit(1);
    }
    strcpy(cmdArg[i++], ptr);
    if (i == MAXARG) {
      break;
    }
    ptr = strsep(&cmdLine, " ");
  }
  return(cmdArg);
}

int main(int argc, char *argv[], char *envp[]) {
  const char* HOME = "HOME=";
  const char* PWD = "PWD=";
  const char* PATH = "PATH=";
  const u_int STD_MOD = S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH; // modifier 644
  char cmdLine[MAXLINE], **cmdArg, **envVars, **hist, *var, *val, *cmd, *frp;
  int i, j, status, debug, varc, cmdc, args, stop_exec, exec, sip, sop, fid, fd[2];
  pid_t pid1, pid2;
  // set debug flag
  i = 1, debug = 0;
  while (i < argc) {
    if (!strcmp(argv[i++], "-d") ) {
      debug = 1;
    }
  }
  // get environment variables
  i = 0, varc = 0;
  envVars = (char **) malloc(sizeof(char *) * (MAXENV + 1));
  while (envp[i] != NULL) {
    envVars[i] = (char *) malloc(sizeof(char) * (strlen(envp[i]) + 1));
    strcpy(envVars[i], envp[i]);
    varc++, i++;
  }
  // allocate memory for history
  cmdc = 0;
  hist = (char **) malloc(sizeof(char *) * (HISTSIZE));
  for (i = 0; i < HISTSIZE; i++) {
    hist[i] = (char *) malloc(sizeof(char *) * (MAXLINE));
  }
  // set other initial values of pipe flag, stdin, and stdout
  stop_exec = 0;
  sip = dup(STDIN_FILENO);
  sop = dup(STDOUT_FILENO);
  printf("bsh> "); // initial prompt
  while (fgets(cmdLine, MAXLINE, stdin) != NULL) {
    cmdLine[strlen(cmdLine) - 1] = '\0';
    exec = 1;
    // add command to hist
    i = cmdc >= HISTSIZE ? HISTSIZE - 1 : cmdc;
    while (--i >= 0) {
      strcpy(hist[i+1], hist[i]);
    }
    strcpy(hist[0], cmdLine);
    cmdc++;
    // parse command, including spacing out tokens
    cmdArg = parseCmd(cmdLine);
    val = (char *) malloc(sizeof(char) * MAXLINE);
    i = 0;
    while (cmdArg[i] != NULL) {
      if ((val = strpbrk(cmdArg[i], "<|>")) != NULL && strlen(cmdArg[i]) > 1) {
        if (val == cmdArg[i]) {
          // "<|>" found as first character
          if (i > MAXARG - 1) {
            free(cmdArg[i]);
            cmdArg[i--] = NULL;
          }
          for (j = i; cmdArg[j] != NULL; j++);
          while (--j > i) {
            cmdArg[j+1] = (char *) malloc(sizeof(char) * (strlen(cmdArg[j])));
            strcpy(cmdArg[j+1], cmdArg[j]);
            free(cmdArg[j]);
          }
          cmdArg[j+1] = (char *) malloc(sizeof(char) * (strlen(val)));
          strcpy(cmdArg[j+1], (val + 1));
          *(val + 1) = '\0';
        } else if (val == cmdArg[i] + strlen(cmdArg[i]) - 1) {
          // "<|>" found as last character
          if (i > MAXARG - 1) {
            free(cmdArg[i]);
            cmdArg[i--] = NULL;
          }
          for (j = i; cmdArg[j] != NULL; j++);
          while (--j > i) {
            cmdArg[j+1] = (char *) malloc(sizeof(char) * (strlen(cmdArg[j])));
            strcpy(cmdArg[j+1], cmdArg[j]);
            free(cmdArg[j]);
          }
          cmdArg[j+1] = (char *) malloc(sizeof(char) * 2);
          *cmdArg[j+1] = *val;
          *(cmdArg[j+1] + 1) = '\0';
          *val = '\0';
        } else {
          // "<|>" found in the middle of the argument
          while (i > MAXARG - 2) {
            free(cmdArg[i]);
            cmdArg[i--] = NULL;
          }
          for (j = i; cmdArg[j] != NULL; j++);
          while (--j > i + 1) {
            cmdArg[j+1] = (char *) malloc(sizeof(char) * (strlen(cmdArg[j])));
            strcpy(cmdArg[j+1], cmdArg[j]);
            free(cmdArg[j]);
          }
          cmdArg[j+2] = (char *) malloc(sizeof(char) * (strlen(val)));
          strcpy(cmdArg[j+2], (val + 1));
          cmdArg[j+1] = (char *) malloc(sizeof(char) * 2);
          *cmdArg[j+1] = *val;
          *(cmdArg[j+1] + 1) = '\0';
          *val = '\0';
        }
      } else {
        i++;
      }
    }
    free(val);
    if (debug) {
      for (i = 0; cmdArg[i] != NULL; i++) {
        printf("\t%d (%s)\n", i, cmdArg[i]);
      }
    }
    // pipe handling
    for (i = 0; cmdArg[i] != NULL && strcmp(cmdArg[i], "|") != 0; i++);
    if (cmdArg[i] != NULL) {
      if (debug) {
        printf("\n\tpipe found\n");
      }
      if (pipe(fd) == 0) {
        pid2 = fork();
        if (pid2 == 0) {
          // child process for the receiving part of the pipeline
          waitpid(pid2, &status, 0);
          for (j = 0; j <= i; j++) {
            free(cmdArg[j]);
            cmdArg[j] = NULL;
          }
          while (cmdArg[j] != NULL) {
            cmdArg[j-i-1] = (char *) malloc(sizeof(char) * (strlen(cmdArg[j]) + 1));
            strcpy(cmdArg[j-i-1], cmdArg[j]);
            free(cmdArg[j]);
            cmdArg[j++] = NULL;
          }
          if (debug) {
            printf("\treceiving process\n");
            for (j = 0; cmdArg[j] != NULL; j++) {
              printf("\t%d (%s)\n", j, cmdArg[j]);
            }
            printf("\n");
          }
          close(fd[1]);
          close(STDIN_FILENO);        
          if (dup(fd[0]) == -1) {
            perror("pipe - could not change input\n");
          }
          close(fd[0]);
          stop_exec = 1;
        } else {
          pid1 = fork();
          if (pid1 == 0){
            // child process for the sending part of the pipeline
            for (j = i; cmdArg[j] != NULL; j++) {
              free(cmdArg[j]);
              cmdArg[j++] = NULL;
            }
            if (debug) {
              printf("\tsending process\n");
              for (j = 0; cmdArg[j] != NULL; j++) {
                printf("\t%d (%s)\n", j, cmdArg[j]);
              }
              printf("\n");
            }
            close(fd[0]);
            close(STDOUT_FILENO);
            if (dup(fd[1]) == -1) {
              perror("pipe - could not change output\n");
            }
            close(fd[1]);
            stop_exec = 1;
          } else {
            // parent process, bsh
            waitpid(pid1, &status, 0);
            close(fd[0]);
            close(fd[1]);
            exec = 0;
          }
        }
      } else {
        printf("could not create pipe\n");
      }
    }
    // execute command
    if (exec) {
      // redirection handling
      i = 1;
      while (cmdArg[i] != NULL) {
        j = i;
        if (strcmp(cmdArg[j], ">") == 0) {
          fid = open(cmdArg[j+1], O_WRONLY | O_CREAT | O_TRUNC, STD_MOD);
          close(1);
          if (dup(fid) == -1) {
            perror(cmdArg[j+1]);
          }
          close(fid);
          while (cmdArg[j+2] != NULL) {
            free(cmdArg[j]);
            cmdArg[j] = (char *) malloc(sizeof(char) * (strlen(cmdArg[j+2]) + 1));
            strcpy(cmdArg[j], cmdArg[j+2]);
            j++;
          }
          free(cmdArg[j+1]);
          cmdArg[j+1] = NULL;
          free(cmdArg[j]);
          cmdArg[j] = NULL;
        } else if (strcmp(cmdArg[j], "<") == 0) {
          fid = open(cmdArg[j+1], O_RDONLY);
          close(0);
          if (dup(fid) == -1) {
            perror(cmdArg[j+1]);
          }
          close(fid);
          while (cmdArg[j+2] != NULL) {
            free(cmdArg[j]);
            cmdArg[j] = (char *) malloc(sizeof(char) * (strlen(cmdArg[j+2]) + 1));
            strcpy(cmdArg[j], cmdArg[j+2]);
            j++;
          }
          free(cmdArg[j+1]);
          cmdArg[j+1] = NULL;
          free(cmdArg[j]);
          cmdArg[j] = NULL;
        } else {
          i++;
        }
      }
      for (args = 0; cmdArg[args] != NULL; args++);
      if (strcmp(cmdArg[0], "exit") == 0) {
        // built-in command exit
        if (debug) {
          printf("\texiting\n");
        }
        break;
      } else if (strcmp(cmdArg[0], "env") == 0) {
        // built-in command env
        if (debug) {
          printf("\tenvironment variables\n");
        }
        for (i = 0; i < varc; i++) {
          printf("%s\n", envVars[i]);
        }
      } else if (strcmp(cmdArg[0], "setenv") == 0) {
        // built-in command setenv
        if (debug) {
          printf("\tsetting environment variable\n");
        }
        if (args >= 3) {
          var = (char *) malloc(sizeof(char) * (strlen(cmdArg[1]) + strlen(cmdArg[2]) + 1));
          frp = var;
          strcpy(var, cmdArg[1]);
          strcat(var, "=");
          strcat(var, cmdArg[2]);
          for (i = 0; i < varc && strncmp(cmdArg[1], envVars[i], strlen(cmdArg[1])) != 0; i++);
          if (i < MAXENV) {
            if (i < varc) {
              free(envVars[i]);
            } else {
              varc++;
            }
            envVars[i] = (char *) malloc(sizeof(char) * (strlen(var) + 1));
            strcpy(envVars[i], var);
          } else {
            printf("setenv: too many environment variables\n");
          }
          free(frp);
        } else {
          printf("not enough arguments\n");
        }
      } else if (strcmp(cmdArg[0], "unsetenv") == 0) {
        // built-in command unsetenv
        if (debug) {
          printf("\tunsetting environment variable\n");
        }
        if (args >= 2) {
          for (i = 0; i < varc && strncmp(cmdArg[1], envVars[i], strlen(cmdArg[1])) != 0; i++);
          if (i < varc) {
            while (++i < varc) {
              free(envVars[i-1]);
              envVars[i-1] = (char *) malloc(sizeof(char) * (strlen(envVars[i]) + 1));
              strcpy(envVars[i-1], envVars[i]);
            }
            free(envVars[--varc]);
          } else {
            printf("unsetenv: environment variable (%s) not found\n", cmdArg[1]);
          }
        } else {
          printf("not enough arguments\n");
        }
      } else if (strcmp(cmdArg[0], "cd") == 0) {
        // built-in command cd
        if (debug) {
          printf("\tchanging directory\n");
        }
        // if there is no filepath argument, set target directory to home
        if (cmdArg[1] == NULL) {
          for (i = 0; i < varc && strncmp(envVars[i], HOME, strlen(HOME)) != 0; i++);
          if (i < varc) {
            var = (char *) malloc(sizeof(char) * (strlen(envVars[i])+1));
            frp = var;
            strcpy(var, envVars[i]);
            var += strlen(HOME);
            cmdArg[1] = (char *) malloc(sizeof(char) * (strlen(var) + 1));
            strcpy(cmdArg[1], var);
            free(frp);
          }
        }
        if (chdir(cmdArg[1]) == 0) {
          for (i = 0; i < varc && strncmp(envVars[i], PWD, strlen(PWD)) != 0; i++);
          var = (char *) malloc(sizeof(char) * MAXLINE);
          val = (char *) malloc(sizeof(char) * MAXLINE);
          frp = var;
          strcpy(var, PWD);
          if (getcwd(val, MAXLINE) != NULL) {
            strcat(var, val);
          }
          if (i < MAXENV) {
            if (i < varc) {
              free(envVars[i]);
            } else {
              varc++;
            }
            envVars[i] = (char *) malloc(sizeof(char) * (strlen(var) + 1));
            strcpy(envVars[i], var);
            free(frp);
            free(val);
          } else {
            printf("cannot update PWD; exiting\n");
            stop_exec = 1;
          }
        } else {
          perror(cmdArg[1]);
        }
      } else if (strcmp(cmdArg[0], "history") == 0) {
        // built-in command history
        if (debug) {
          printf("\thistory\n");
        }
        for (i = cmdc >= HISTSIZE ? HISTSIZE : cmdc; i > 0; i--) {
          printf("%5d  %s\n", cmdc - i + 1, hist[i-1]);
        }
      } else {
        // executing Minix commands
        if (debug) {
          printf("\texecuting command (%s)\n", cmdArg[0]);
        }
        if (strpbrk(cmdArg[0], "/") != NULL) {
          if (access(cmdArg[0], X_OK) == 0) {
            // run an executable specified by filepath
            pid1 = fork();
            if (pid1 != 0) {
              waitpid(pid1, &status, 0);
            } else {
              execv(cmdArg[0], cmdArg);
            }
          } else {
            perror(cmdArg[0]);
          }
        } else {
          // run an executable found in the PATH environment variable
          for (i = 0; i < varc && strncmp(envVars[i], PATH, strlen(PATH)) != 0; i++);
          if (i < varc) {
            var = (char *) malloc(sizeof(char) * (strlen(envVars[i]) + 1));
            frp = var;
            strcpy(var, envVars[i]);
            var += strlen(PATH);
            cmd = (char *) malloc(sizeof(char) * MAXLINE);
            while ((val = strsep(&var, ":")) != NULL) {
              strcpy(cmd, val);
              strcat(cmd, "/");
              strcat(cmd, cmdArg[0]);
              if (access(cmd, X_OK) == 0) {
                pid1 = fork();
                if (pid1 != 0) {
                  waitpid(pid1, &status, 0);
                } else {
                  execv(cmd, cmdArg);
                }
                break;
              }
              val = NULL;
            }
            if (val == NULL) {
              perror(cmdArg[0]);
            }
            free(cmd);
            free(frp);
          } else {
            printf("PATH variable not found\n");
          }
        }
      }
    }
    // clean up before running the next command
    i = 0;
    while (cmdArg[i] != NULL) {
      free(cmdArg[i++]);
    }
    free(cmdArg);
    // do not continue if this is a pipe subprocess
    if (!stop_exec) {
      close(0);
      if (dup(sip) == -1) {
        perror("stdin");
        break;
      }
      close(1);
      if (dup(sop) == -1) {
        perror("stdout");
        break;
      }
      printf("bsh> ");
    }
    else {
      break;
    }
  }
  // free up hist variables when done
  for (i = 0; i < HISTSIZE; i++) {
    free(hist[i]);
  }
  free(hist);
  return 0;
}
