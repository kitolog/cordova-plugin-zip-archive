cordova-plugin-zip-archive
============================
Cordova plugin to create zip archive 

### Installation
cordova plugin add cordova-plugin-zip-archive

// you may also install directly from this repo
cordova plugin add https://github.com/kitolog/cordova-plugin-zip-archive
 
## Sample usage
Here is simple example of how to zip files

Create instance of Zip plugin:
```
var zipInstance = new window.zipArchive()
```

Set data consumer, error and close handlers:
```
zipInstance.onError = function(errorMessage) {
  // invoked after error occurs
};
```
Archive files
```
zipInstance.zip('/PATH/TO/NEW/ARCHIVE.zip',
    ['/PATH/TO/FILE1.txt',
     '/PATH/TO/FILE2.txt'
     '/PATH/TO/FILE3.txt'
     '/PATH/TO/FILE4.csv'
    ], 
    () => {
    // zip archive created
    },
    (error) => {
    // handle errors
    })
```

## API
### Event handlers
#### `onError: (message: string) => void`
Invoked when some error occurs during zip process.

#### `on: (eventName: string, callback: function) => void`
Syntax sugar for the event handlers (onTick, onStop, onError)
eventName: `error` 

### Methods
#### `zip(path, files, onSuccess?, onError?): void`
Establishes connection with the remote host.

| parameter   | type          | description |
| ----------- |-----------------------------|--------------|
| `path`     | `string`                    | zip archive path | |
| `files`  | `string array`                    | path to files |
| `onSuccess` | `() => void`                | Success callback - called after archive was created. (optional)|
| `onError`   | `(message: string) => void` | Error callback - called when some error occurs during creating an archive. (optional)|

## What's new
 - 1.0.0 - initial code