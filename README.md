# codeBeamer-examples

## Usage
Set variable `CB_HOME` e.g. in `%HOMEPATH%/.gradle/gradle.properties` to point to your local codeBeamer installation.  

Use `gradlew eclipse` to create project configuration for Eclipse.

Use `gradlew build` to create distribution files that can be installed into the codeBeamer.

## Examples

### Custom REST API

Does  deploy a custom REST endpoint that can be used to move trackers into folder.
Documentation is automatically added to the Swagger documentation (CODEBEAMER/v3/swagger/editor.spr#/Custom/moveTracker)

### Tracker Item Listener

Adds a listener that appends some text once a Test Run reaches the runnable state.
