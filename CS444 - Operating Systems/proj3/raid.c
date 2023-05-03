#include <stdio.h>
#include <string.h>
#include <unistd.h>

int main(int argc, char *argv[]) {
    FILE *fp = NULL;
    char inname[255] = "test.txt";
    int opt;
    while((opt = getopt(argc, argv, "f:")) != -1) {
        if(opt == 'f') {
            strcpy(inname, optarg);
        }
    }
    fp = fopen(inname, "r");
    if (fp != NULL) {
        int i, j; 
        int count = 0;
        int n = strlen(inname) + 5;
        char outname[255] = {0};
        strncat(outname, inname, 255);
        strncat(outname, ".part", 5);
        FILE *f[7];
        for(j = 0; j < 7; j++) {
            outname[n] = (j + '0');
            f[j] = fopen(outname, "wb");
        }
        char c;
        unsigned char lo, hi;
        unsigned char towrite[7];
        unsigned char pd[7];
        while(1) {
            for(j = 0; j < 7; j++) {
                towrite[j] = 0;
            }
            for(i = 0; i < 4; i++) {
                if((c=fgetc(fp)) == EOF) {
                    break;
                }
                count++;
                lo = c & 15;
                hi = (c >> 4) & 15;
                pd[6] = (hi & 1) >> 0;
                pd[5] = (hi & 2) >> 1;
                pd[4] = (hi & 4) >> 2;
                pd[2] = (hi & 8) >> 3;
                pd[0] = pd[2] ^ pd[4] ^ pd[6];
                pd[1] = pd[2] ^ pd[5] ^ pd[6];
                pd[3] = pd[4] ^ pd[5] ^ pd[6];
                for(int j = 0; j < 7; j++) {
                    towrite[j] ^= pd[j] << (7 - (2 * i));
                }
                pd[6] = (lo & 1) >> 0;
                pd[5] = (lo & 2) >> 1;
                pd[4] = (lo & 4) >> 2;
                pd[2] = (lo & 8) >> 3;
                pd[0] = pd[2] ^ pd[4] ^ pd[6];
                pd[1] = pd[2] ^ pd[5] ^ pd[6];
                pd[3] = pd[4] ^ pd[5] ^ pd[6];
                for(int j = 0; j < 7; j++) {
                    towrite[j] ^= pd[j] << (6 - (2 * i));
                }
            }
            if(i != 0) {
                for(j = 0; j < 7; j++) {
                    fputc(towrite[j], f[j]);
                }
            }
            if(c == EOF) {
                break;
            }
        }
        for (j = 0; j < 7; j++) {
            fclose(f[j]);
        }
        fclose(fp);
        printf("%s: %d B\n", inname, count);
    } else {
        printf("no file %s\n", inname);
        return 1;
    }
    return 0;
}
