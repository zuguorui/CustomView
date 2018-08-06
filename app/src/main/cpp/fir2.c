//
//  fir2.c
//  Fir2
//
//  Created by Yudong He on 2018/4/3.
//  Copyright © 2018 Yudong He. All rights reserved.
//
#include "fir2.h"

void idft( float complex *H, int N, float complex *h ){
	int i,j;
	float complex sum;
	for(i = 0; i < N; i++){
		sum = 0;
		for(j = 0; j < N; j++){
			sum = sum + H[j] * cexp(I * 2 * PI * i * j / N);
		}
		h[i] = sum / N;
	}
}


float *eqfilter(int N, int sr, float *banks, float *eqfactors, int numbanks)
{
	/*Parameter*/
	int M, i, j;
	M = 2 * N;


	/*transfer frequencies in banks to position points in nbanks*/
	float *nbanks = (float *)calloc(numbanks, sizeof(float));
	for(i=0; i < numbanks; i++){
		nbanks[i] = M * banks[i] / (sr / 2);
	}

	/*transfer db in eqfactors to amplitude factor in eq*/
	float *eq = (float *)calloc(numbanks, sizeof(float));
	for(i = 0; i < numbanks; i++){
		eq[i] = pow(10, eqfactors[i] / 20);
	}

	/*calculate ideal amplitude frequency response*/
	float *FILTER = (float *)calloc(M, sizeof(float));
	for(j = 0; j < (int)((nbanks[0] + nbanks[1]) / 2 + 0.5);  j++){
		FILTER[j] = eq[0];
	}
	for(i = 1; i < numbanks - 1; i++){
		for(; j < (int)((nbanks[i] + nbanks[i + 1]) / 2 + 0.5); j++){
			FILTER[j] = eq[i];
		}
	}
	for(; j < M; j++){
		FILTER[j] = eq[i];
	}
	/*calculate rad and frequency response*/
	float complex rad = 0;
	float complex *Filter = (float complex*)calloc(2 * M - 2, sizeof(float complex));
	for(i = 0; i < M; i++){
		rad = -1 * (N / 2) * I * PI * i / (M - 1);
		Filter[i] = FILTER[i] * cexp(rad);
	}
	for(; i < 2 * M - 2; i++){
		Filter[i] = conj(Filter[2 * M - 2 - i]);
	}
	/*calculate hamming function*/
	float *hamming = (float *)calloc(N, sizeof(float));
	float alpha = 0.46;
	for(i = 0; i < N; i++){
		hamming[i] = (1 - alpha) - alpha * cos(2 * PI * i / (N - 1));
	}
	/*calculate pulse response*/
	float complex *filter_d = (float complex *)calloc(2 * M - 2, sizeof(float complex));
	idft(Filter, 2 * M - 2, filter_d);

	/*cut off filter_d by hamming window*/
	float *filter = (float *)calloc(N, sizeof(float));
	for(i = 0; i < N; i++){
		filter[i] = creal(filter_d[i]) * hamming[i];
	}

	/*free buff*/
	free(nbanks);
	free(eq);
	free(FILTER);
	free(Filter);
	free(hamming);
	/*return filter pointer*/
	return filter;

}

