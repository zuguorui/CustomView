//
//  fir2.h
//  Fir2
//
//  Created by Yudong He on 2018/4/3.
//  Copyright © 2018 Yudong He. All rights reserved.
//

#ifndef fir2_h
#define fir2_h
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <complex.h>


#ifndef PI
#define PI	3.14159265358979323846264338327950288
#endif

#define SIZE_OF_SUM_FILTER_256  256
#define SampleRate 44100L

void idft(float complex *H, int N, float complex *h );


float *eqfilter(int N, int sr, float *banks, float *eqfactors, int numbanks);

#endif
