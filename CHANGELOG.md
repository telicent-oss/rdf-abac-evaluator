# REST Service for RDF Attribute Based Access Control evaluation

# 1.2.8
- RDF ABAC upgraded from 1.1.4 to 3.1.6, which brings Apache Jena 6.2.0 (was 5.6.0)
    - `ALEServer` and `SASServer` now extend `CmdMain`, as `exec()`/`mainRun()` are no longer on `CmdGeneral`
    - Jetty upgraded to 12.1.12 and moved from the Jakarta EE 10 to the EE 11 flavour, matching Jena 6.x
- Caffeine is now declared explicitly (3.2.4) as it is used directly by the cached attribute store
- Removed `mockito-inline`: the inline mock maker has been the default since Mockito 5
- Various build and test dependencies upgraded to latest available

# 1.2.7
- Build improvement

# 1.2.6
- Build improvement

# 1.2.5
- Build improvement

# 1.2.4
- Build improvements

# 1.2.3
- Update Telicent Base Image

# 1.2.2
- Using Telicent Base Image

# 1.2.1 
- Minor fixes to process following v1.2.0 - including missing change log.

# 1.2.0
- First Open Source commit.