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

#import <Foundation/Foundation.h>

@interface ZipArchiveAdapter : NSObject <NSStreamDelegate> {
@public
}

- (void)zip:(NSString *)path files:(NSArray<NSString *> *)files;
- (void)stop;

@property (copy) void (^zipEventHandler)(NSString*);
@property (copy) void (^zipErrorEventHandler)(NSString*);
@property (copy) void (^progressHandler)(NSNumber*);
@property (copy) void (^stopEventHandler)(BOOL);
@property (copy) void (^errorEventHandler)(NSString*, NSString *);

@end
