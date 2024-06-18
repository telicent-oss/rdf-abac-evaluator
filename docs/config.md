## Configuration

The following values can be configured in either command line arguments or in a configuration file:

#### --config | conf | configuration | configfile | configFile | c

The URI path to a JSON file containing config values.

For example ```--config "file:src/test/resources/test_config.json"```

#### --store | attrStore | attrstore | store | attributeStore | attributestore | attributes | userAttrStore

The URI path to either a file or endpoint representing the user/hierarchy information.

For example:

```--store "file:src/main/resources/sample_attributes.ttl"```

```--store "http://localhost:64331"```

#### --port | p

The port number to be used for the server. Defaults to ```64431```.

#### --cachingEnabled | cacheEnabled | cache | enableCache | enableCaching | cachingEnabled

Enables caching of user attributes and hierarchy data.

For example: ```cacheEnabled true```
#### --cacheexpiry

Used to determine how long to cache data.

Defaults to ```PT10S```

#### --userendpoint

Optional URL if user attribute endpoint differs from the given URL and default.

Defaults to ```"/users/lookup/{user}"```

#### --hierarchyendpoint

Optional URL if hierarchy endpoint differs from the given URL (and default).

Defaults to ```"/hierarchies/lookup/{name}"```

### Note: Simple Attribute Store
When using the above configuration, in order to better simulate connecting to the Access server, we can run the Simple Attribute Server (as described above).
It will use a file (similar to the Attribute Label Evaluator when run in file mode).