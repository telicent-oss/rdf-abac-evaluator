## RDF (Resource Description Framework)
RDF is a standardized framework of structuring information to make it easier to describe and link data.

### Resources
In RDF, everything is considered a resource. This can be anything from a web page to a person to a concept like "love" or "happiness".

### Uniform Resource Identifiers (URIs)
Each resource is identified by a unique URI. This helps in distinguishing between different resources and ensures greater clarity in data.
The most well known form of URI is URL, like https://telicent.io/, which is technically a subset.

### Triples
RDF represents information in the form of triples, which as the name suggests consist of three parts:
subject, predicate, and object.
- **Subject**: The resource you're talking about.
- **Predicate**: The property or relationship of the resource.
- **Object**: The value of that property or the resource it's related to.

For example, let's say we want to represent the fact that *"Ian likes dogs"*. In RDF that would be:
- Subject: Ian
- Predicate: likes
- Object: dogs

So, in RDF syntax, it would look like: `<http://example.com/ian> <http://example.com/likes> <http://example.com/dogs> .`

### Graph Structure
RDF forms a graph structure where resources are nodes, and the predicates are the edges connecting them.
This allows for a more flexible and interconnected form of data representation.

### Interoperability
RDF enables data from different sources to be combined and understood together, facilitating interoperability across various applications and domains.

