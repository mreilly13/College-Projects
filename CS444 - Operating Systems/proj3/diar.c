#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

int main(int argc, char *argv[]) {
    char basename[255] = "test.txt";
    int s = 0, ff = 0;
    int opt;
    while((opt = getopt(argc, argv, "f:s:")) != -1) {
        switch (opt) {
            case 'f':
                strcpy(basename, optarg);
                ff = 1;
                break;
            case 's':
                s = atoi(optarg);
                break;
            default:
                break;
        }
    }
    if (!ff) {
        s = 16;
    }
    int i, j;
    int missing = 0;
    FILE *f[7] = {0};
    char raidfilename[255];
    strcpy(raidfilename, basename);
    strncat(raidfilename, ".part", 5);
    int n = strlen(raidfilename);
    for(j = 0; j < 7; j++) {
        raidfilename[n] = (j + '0');
        f[j] = fopen(raidfilename, "rb");
        if(f[j] == NULL) {
            printf("no file %s\n", raidfilename);
            missing = 1;
            break;
        }
    }
    if(missing == 0) {
        char outfilename[255] = {0};
        strcpy(outfilename, basename);
        strncat(outfilename, ".2", 2);
        FILE *fp = fopen(outfilename, "w"); 
        unsigned char c;
        unsigned char pd[7];
        unsigned char p1, p2, p4;
        int code;
        for(i = 0; i < s/4; i++) {
            for(j = 0; j < 7; j++) {
                pd[j] = fgetc(f[j]);
            }
            p1 = pd[2] ^ pd[4] ^ pd[6];
            p2 = pd[2] ^ pd[5] ^ pd[6];
            p4 = pd[4] ^ pd[5] ^ pd[6];
            if(p1 != pd[0] || p2 != pd[1] || p4 != pd[3]) {
                for(j = 7; j >= 0; j--) {
                    code = 0;
                    code ^= ((p1 & 1 << j) ^ (pd[0] & 1 << j)) << (7-j) >> 7;
                    code ^= ((p2 & 1 << j) ^ (pd[1] & 1 << j)) << (7-j) >> 6;
                    code ^= ((p4 & 1 << j) ^ (pd[3] & 1 << j)) << (7-j) >> 5;
                    if(code-- != 0) {
                        pd[code] ^= 1 << j;
                    }
                }
            }
            for(j = 0; j < 4; j++) {
                c = 0;
                c ^= (pd[2] & 1 << (7-2*j)) << (2*j) >> 0;
                c ^= (pd[4] & 1 << (7-2*j)) << (2*j) >> 1;
                c ^= (pd[5] & 1 << (7-2*j)) << (2*j) >> 2;
                c ^= (pd[6] & 1 << (7-2*j)) << (2*j) >> 3;
                c ^= (pd[2] & 1 << (6-2*j)) << (2*j+1) >> 4;
                c ^= (pd[4] & 1 << (6-2*j)) << (2*j+1) >> 5;
                c ^= (pd[5] & 1 << (6-2*j)) << (2*j+1) >> 6;
                c ^= (pd[6] & 1 << (6-2*j)) << (2*j+1) >> 7;
                fputc(c, fp);
            }
        }
        fclose(fp);
    }
    for (j = 0; j < 7; j++) {
        if(f[j] != NULL) {
            fclose(f[j]);
        }
    }
    if(missing == 0) {
        printf("%s: %d B\n", basename, s);
        return 0;
    } else {
        return 1;
    }
}
