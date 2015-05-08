//
//  FingerDetection.m
//  TGV
//
//  Created by Catie Baker on 6/3/13.
//
//
// Copyright (c) 2012-2013 University of Washington
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// - Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// - Neither the name of the University of Washington nor the names of its
// contributors may be used to endorse or promote products derived from this
// software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
// OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//


#import "FingerDetection.h"

#define min(a,b)
#define Threshold 1000

#if TARGET_MAC
#define R 0
#define G 1
#define B 2
#else
#define R 2
#define G 1
#define B 0
#endif

#define BPP 4



@implementation FingerDetection

/*create a black and white bitmap with black = within threshold, 
 *white = not within threshold
 *If a pixel is within the threshold, finds the closest blob 
 *and if necessary updates closestBlob
 */
+(BOOL) findFingerinBitmap: (uint8_t *)bitmap
                  toBitmap: (uint8_t *)debugBitmap
                     width: (size_t) width
                    height: (size_t) height
                   fingerX: (int *) fingerX
                   fingerY: (int *) fingerY
                  andBlobs: (NSArray *) QRBlobs
                      Blob:(Blob **)closestBlob {
    uint8_t *p1;
    uint8_t *p2;
    uint8_t *squarePx;
    uint8_t *endBit = bitmap +width*height*BPP;
    int count = 0;
    int minDist = INFINITY;
    int x = 0;
    int y = 0;
    int dist = 0;
    int blobNum = 0;
    BOOL found = NO;
    int inSquare = 0;
    //printf("before loop\n");
    for( p1 = bitmap, p2=debugBitmap; p1<endBit; p1+= BPP, p2+=BPP, count++){
        //printf("entered bitmap loop\n");
        if ( [self testThresholdred:p1[R] green: p1[G] blue: p1[B]]) {
            p2[R] = 0;
            p2[G] = 0;
            p2[B] = 0;
            x = count%width;
            y = count/width;
            blobNum = 0;
            for(Blob *blob in QRBlobs){
                dist = [self distToClosestBlob: blob fromX: x andY: y];
                if(dist<minDist){
                    inSquare = 0;
                    for(int j = max(0,x-5); j<min(width,x+5);j++){
                        for(int k = max(0,y-5); k<min(height,y+5); k++){
                            squarePx = bitmap + ( k*width + j)*BPP;
                            if ( [self testThresholdred:squarePx[R] green: squarePx[G] blue: squarePx[B]]) {
                                inSquare++;
                                //squarePx[R] = 125;
                                //squarePx[G] = 125;
                            }
                        }
                    }
                    
                    //printf("In square: %d\n",inSquare);
                    if(inSquare > 25){
                        minDist = dist;
                        *closestBlob = blob;
                        //printf("updated blob to blob %d\n", blobNum);
                        *fingerX = x;
                        *fingerY = y;
                        found = YES;
                    }
                    
                }
                blobNum++;
            }
            
            
        }
        else{
            p2[R] = 255;
            p2[G] = 255;
            p2[B] = 255;
        }
    }
    
    
    //printf("closest blob: minX %u, minY %u, maxX %u, maxY %u \n", (*closestBlob).minx, (*closestBlob).miny, (*closestBlob).maxx, (*closestBlob).maxy);
    //printf("finger at %d, %d\n",*fingerX,*fingerY);
    return found;
    
}


/**Looks at the midpoints of the square created by minx, miny, maxx, and maxy. Finds the min distance of x,y to one of the midpoints
 
 */
+(int) distToClosestBlob: (Blob *) blob
                          fromX: (int) x
                           andY: (int) y{
    
 
    return pow(((blob.maxx+blob.minx)/2) - x,2) + pow(((blob.maxy+blob.miny)/2)-y,2);
        
    
    //return minDist;
}

//checks whether a pixel is within a threshold
+(BOOL) testThresholdred: (uint8_t) red green: (uint8_t) green blue: (uint8_t) blue
{
    return (pow(red-green,2) + pow(red-blue,2) + pow(green-blue, 2) ) > Threshold;
    
}


@end
