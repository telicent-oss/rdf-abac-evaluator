## Further work

#### Security
At present there is no security in place for the calls made to the server.
In order to do so, changes will either need to be made to the rdf-abac repo to allow this (see later point) or we will need to bring the underlying server
code into this repo.

#### Additional endpoint
Add another endpoint that is capable of taking an array of labels to evaluate for a given user.

#### Additional caching
At present the caching is only focused on the user/hierarchy information and not the evaluated results.
There is a valid argument that those details should be cached on the client side and thus such calls not made to the server but for a belt n braces approach it's something to consider.

#### Metrics
In order to track the server's usage to help guide configuration etc... we will need metrics. The caffeine cache has in built metrics that we should also source.

#### Increase test data
Right now there is only a limited number of users and hierarchies in the data sets used.  This needs to be increased in order to allow for greater flexibility in our testing.

#### Extend WireMock
Again, at present the Wiremock mappings are a little limited - a small fixed delay of 10ms, and the same response for each call. We should extend this to do more - random delays and smarter responses (depending on the input).

#### Potential rdf-abac repo changes
Allow for greater control of configuration with the underlying Jetty Server. At present, we use a pre-configured setup as per Jena testing so a minimum of 2 threads and maximum of 20.
This is fine for now but going forward, and once we understand the behavior better, we are going to want to tweak this, which we can't currently do.  
