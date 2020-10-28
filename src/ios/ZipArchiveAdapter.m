/**
 * Copyright (c) 2018, kitolog
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by kitolog. The name of the
 * kitolog may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */

#import "ZipArchiveAdapter.h"
#import "SSZipArchive.h"

@implementation ZipArchiveAdapter

- (void)zip:(NSString *)path files:(NSArray<NSString *> *)files maxSize:(float)maxSize {

    NSLog(@"[ZipArchive] ZIP path: %@", path);
    NSLog(@"[ZipArchive] ZIP maxSize: %f", maxSize);

//    + (BOOL)createZipFileAtPath:(NSString *)path withFilesAtPaths:(NSArray<NSString *> *)paths
    int diskSize = (int) (maxSize * 1024 * 1024);
    BOOL success = [SSZipArchive createZipFileAtPath:path withFilesAtPaths:files diskSize:diskSize];
    if (success) {
        NSLog(@"Success zip");
        self.zipEventHandler(path);
    } else {
        NSLog(@"No success zip");
        self.zipErrorEventHandler(@"Zip process error");
    }
}

-(void)onProgressTick:(NSNumber *)progress {
//    tick++;
//    NSLog(@"[ZIP] On timer tick: %d", tick);
//    NSNumber* tickNumber = [NSNumber numberWithInteger:tick];
//    self.progressHandler(tickNumber);
}

- (void)stop {
    NSLog(@"[ZIP] instance STOP");
    self.stopEventHandler(FALSE);
}

@end
