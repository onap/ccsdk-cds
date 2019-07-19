.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

Expression
==========

TOSCA provides for a set of functions to reference elements within the template or to retrieve runtime values.

Below is a list of supported expressions

get_input
---------

The get_input function is used to retrieve the values of properties declared within the inputs section of a TOSCA Service Template.

http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454178

get_property
------------

The get_property function is used to retrieve property values between modelable entities defined in the same service template.

http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454178

get_attribute
-------------

The get_attribute function is used to retrieve the values of named attributes declared by the referenced node or relationship template name.

http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454179

get_operation_output
--------------------

The get_operation_output function is used to retrieve the values of variables exposed / exported from an interface operation.

http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454180

get_artifact
------------

The get_artifact function is used to retrieve artifact location between modelable entities defined in the same service template.

http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454182
