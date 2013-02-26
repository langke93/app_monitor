<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
String path = request.getContextPath();
%>
<head>
	<title></title>
	<link href="<%=path %>/css/frame.css" rel="stylesheet" type="text/css" />
</head>
<body class="side">
	<div class="menu_box">
		<h2 onclick="toggleMenu('app monitor')">app monitor</h2>
		<ul id="app monitor" style="display:none;">
			<li><a href="<%=path %>/monitor/_report" target="mainFrame">summary report</a></li>
		</ul>
	</div>
	<c:forEach items="${list}" var="list">
	<div class="menu_box">
		<h2 onclick="toggleMenu('${list.name}')">${list.name }</h2>
		<ul id="${list.name }" style="display:none;">
		<c:forEach items="${list.childen }" var="list">
		<c:if test="${empty list.extend}">
			<li><a href="${list.url }" target="mainFrame">${list.name }</a></li>
		</c:if>
		<c:if test="${!empty list.extend}">
			<li><a href="<%=path %>/menu/url.do?id=${list.id }" target="mainFrame">${list.name }</a></li>
		</c:if>
		</c:forEach>
		</ul>
	</div>
	</c:forEach>
	
	<div class="menu_box">
		<h2 onclick="toggleMenu('settings')">settings</h2>
		<ul id="settings" style="display:none;">
			<li><a href="<%=path %>/menu/menuList.do" target="mainFrame">menu manager</a></li>
		</ul>
	</div>
	
	<script type="text/javascript">
		function toggleMenu(menuId) {
			var menu = document.getElementById(menuId);
			if(menu.style.display == "none") {
				menu.style.display = "";
			}else {
				menu.style.display = "none";
			}
		}
	</script>
</body>
</html>

