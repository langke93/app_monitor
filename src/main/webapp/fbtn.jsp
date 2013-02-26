<%@page contentType="text/html; charset=utf-8"%>
<%
String path = request.getContextPath();
%>
<head>
	<title></title>
	<link href="<%=path %>/css/frame.css" rel="stylesheet" type="text/css" />
	<script type="text/javascript">
		var isHidden = false;
		function hideMenu() {
			if(isHidden) {
				isHidden = false;
				document.getElementById("hideBtn").className = "close";
				self.parent.document.getElementById("workspace").cols = "15%, 8, *";
			}else {
				document.getElementById("hideBtn").className = "open";
				self.parent.document.getElementById("workspace").cols = "0, 8, *";
				isHidden = true;
			}
		}
	</script>
</head>
<body class="fbtn">
	<a id="hideBtn" class="close" href="javascript:void(0)" onclick="hideMenu()"></a>
</body>
</html>
