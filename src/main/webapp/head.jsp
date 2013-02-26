<%@page contentType="text/html; charset=utf-8"%>
<%@page import="java.util.*"%>
<%
String path = request.getContextPath();
%>
<html>
<head>
<title></title>
<link href="<%=path %>/css/frame.css" rel="stylesheet" type="text/css" />
</head>
<body class="hd">
	<div class="logo"><%=org.langke.jetty.common.Config.get().get("jsp.logo") %></div>
	<div class="user">
	</div>
</body>
</html>
