<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://openfaces.org/" prefix="o" %>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<html>
<head>
  <title>Chart with o:ajax</title>
  <link rel="STYLESHEET" type="text/css" href="../../main.css"/>
</head>

<body>
<f:view>
  <h:form id="formID">
   <%@ include file="chart_q_reload_components_core.xhtml" %>
  </h:form>
</f:view>

</body>
</html>