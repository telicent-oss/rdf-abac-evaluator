## ABAC (Attribute-Based Access Control)
ABAC is an access control model that defines permissions based on attributes associated with entities such as users, resources, and environmental conditions.
In ABAC, access control decisions are made by evaluating the attributes of these entities against a set of policies or criteria.

### Attributes
Attributes are characteristics or properties associated with entities in the system.
These attributes could include user roles, resource types, time of access, location, and any other relevant information.

For example, we can think of government security classifications: Official, Secret, Top Secret; as an attribute that
could be used to determine access.

### Policies
Policies define the rules or conditions that determine access permissions based on the attributes of the entities involved.
Policies can be expressed using logical expressions, rules, or other formalisms to specify who can access what under which conditions.

### Evaluation Engine
ABAC relies on an evaluation engine to make access control decisions.
This engine evaluates the attributes of the requesting entity, the resource being accessed, and any relevant environmental conditions against the defined policies to determine whether access should be granted or denied.

### Dynamic and Contextual Access Control
ABAC enables dynamic and contextual access control by considering various
attributes and conditions at the time of access request. This allows for more fine-grained and flexible access control compared to traditional models like Role-Based Access Control (RBAC) or Discretionary Access Control (DAC).

### Scalability and Flexibility
ABAC offers scalability and flexibility in managing access control policies.
Since permissions are based on attributes rather than predefined roles or permissions, ABAC can accommodate complex and evolving organizational structures, diverse user roles, and changing access requirements more effectively.

### Integration
ABAC can be integrated with various identity and access management systems, directory services, and applications.
This integration allows organizations to leverage existing attribute data and infrastructure to implement and enforce access control policies.

Overall, Attribute-Based Access Control provides a more dynamic, granular, and context-aware approach to access control, which is particularly useful in complex and dynamic environments where access requirements may vary based on diverse factors.
