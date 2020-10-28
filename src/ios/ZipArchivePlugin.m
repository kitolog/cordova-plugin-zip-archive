/**
 * Copyright (c) 2019, kitolog
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

#import "ZipArchivePlugin.h"
#import "ZipArchiveAdapter.h"
#import <cordova/CDV.h>
#import <Foundation/Foundation.h>

@implementation ZipArchivePlugin : CDVPlugin

- (void) zip : (CDVInvokedUrlCommand*) command {
    
    NSLog(@"[ZipArchive] zip call");
    NSString *adapterKey = [command.arguments objectAtIndex:0];
    NSString *path = [command.arguments objectAtIndex:1];
    NSArray<NSString *> *files = [command.arguments objectAtIndex:2];
    NSNumber *maxSizeNumber = [command.arguments objectAtIndex:3];
    
    float maxSize = [maxSizeNumber floatValue];

    NSLog(@"[ZipArchive] start %@", adapterKey);
    if (adapterInstances == nil) {
        self->adapterInstances = [[NSMutableDictionary alloc] init];
    }

    __block ZipArchiveAdapter* adapterInstance = [[ZipArchiveAdapter alloc] init];

    adapterInstance.zipEventHandler = ^ void (NSString *path) {
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:path] callbackId:command.callbackId];

        [self->adapterInstances setObject:adapterInstance forKey:adapterKey];

        [self removeAdapter:adapterKey];
        adapterInstance = nil;
    };

    adapterInstance.zipErrorEventHandler = ^ void (NSString *error){
        NSLog(@"[ZipArchive] startErrorEventHandler");
        [self.commandDelegate
         sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error]
         callbackId:command.callbackId];
        [self removeAdapter:adapterKey];
        adapterInstance = nil;
    };

    adapterInstance.errorEventHandler = ^ void (NSString *error, NSString *errorType){
        NSMutableDictionary *errorDictionaryData = [[NSMutableDictionary alloc] init];
        [errorDictionaryData setObject:@"OnError" forKey:@"type"];
        [errorDictionaryData setObject:errorType forKey:@"errorType"];
        [errorDictionaryData setObject:error forKey:@"errorMessage"];
        [errorDictionaryData setObject:adapterKey forKey:@"adapterKey"];

        [self dispatchEventWithDictionary:errorDictionaryData];
        [self removeAdapter:adapterKey];
        adapterInstance = nil;
    };
    adapterInstance.progressHandler = ^ void (NSNumber* data) {
        NSMutableDictionary *dataDictionary = [[NSMutableDictionary alloc] init];
        [dataDictionary setObject:@"OnTick" forKey:@"type"];
        [dataDictionary setObject:data forKey:@"data"];
        [dataDictionary setObject:adapterKey forKey:@"adapterKey"];

        [self dispatchEventWithDictionary:dataDictionary];
    };
    adapterInstance.stopEventHandler = ^ void (BOOL hasErrors) {
        NSLog(@"[ZipArchive] stopEventHandler");
        NSMutableDictionary *closeDictionaryData = [[NSMutableDictionary alloc] init];
        [closeDictionaryData setObject:@"OnStop" forKey:@"type"];
        [closeDictionaryData setObject:(hasErrors == TRUE ? @"true": @"false") forKey:@"hasError"];
        [closeDictionaryData setObject:adapterKey forKey:@"adapterKey"];

        [self dispatchEventWithDictionary:closeDictionaryData];
        adapterInstance = nil;
        [self removeAdapter:adapterKey];
    };

    [self.commandDelegate runInBackground:^{
        @try {
            [adapterInstance zip:path files:files maxSize:maxSize];
        }
        @catch (NSException *e) {
            [self.commandDelegate
             sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:e.reason]
             callbackId:command.callbackId];

            adapterInstance = nil;
        }
    }];
}

- (void) stop:(CDVInvokedUrlCommand *) command {

    NSString* adapterKey = [command.arguments objectAtIndex:0];

    ZipArchiveAdapter *adapterInstance = [self getAdapter:adapterKey];

    [self.commandDelegate runInBackground:^{
        @try {
//            if (adapterInstance != nil) {
//                [adapterInstance stop];
//            }else{
//                NSLog(@"[ZipArchive] Stop: adapterInstance is nil. adapterKey: %@", adapterKey);
//            }

            [self.commandDelegate
             sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK]
             callbackId:command.callbackId];
        }
        @catch (NSException *e) {
            [self.commandDelegate
             sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:e.reason]
             callbackId:command.callbackId];
        }
    }];
}

- (void) setOptions: (CDVInvokedUrlCommand *) command {
}

- (ZipArchiveAdapter*) getAdapter: (NSString*) adapterKey {
    ZipArchiveAdapter* adapterInstance = [self->adapterInstances objectForKey:adapterKey];
    if (adapterInstance == nil) {
        NSLog(@"[ZipArchive] Cannot find adapterKey: %@. Connection is probably closed.", adapterKey);
        //NSString *exceptionReason = [NSString stringWithFormat:@"Cannot find adapterKey: %@. Connection is probably closed.", adapterKey];

        //@throw [NSException exceptionWithName:@"IllegalArgumentException" reason:exceptionReason userInfo:nil];
    }
    return adapterInstance;
}

- (void) removeAdapter: (NSString*) adapterKey {
    NSLog(@"[ZipArchive] Removing adapter from storage.");
    [self->adapterInstances removeObjectForKey:adapterKey];
}

- (BOOL) adapterExists: (NSString*) adapterKey {
    ZipArchiveAdapter* adapterInstance = [self->adapterInstances objectForKey:adapterKey];
    return adapterInstance != nil;
}

- (void) dispatchEventWithDictionary: (NSDictionary*) dictionary {
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dictionary options:0 error:nil];
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];

    [self dispatchEvent:jsonString];
}

- (void) dispatchEvent: (NSString *) jsonEventString {
    NSString *jsToEval = [NSString stringWithFormat : @"window.zipArchive.dispatchEvent(%@);", jsonEventString];
    [self.commandDelegate evalJs:jsToEval];
}

@end
