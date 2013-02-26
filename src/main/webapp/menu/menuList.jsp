<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<% 
	String path = request.getContextPath();
%>
<!DOCTYPE c:out PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<script type="text/javascript" src="<%=path %>/js/jquery-1.4.2.js"></script>
	<script type="text/javascript">
		function edit(id,f_id,name,url,orders,extend){
			$("#id").val(id);
			$("#f_id").val(f_id);
			$("#name").val(name);
			$("#url").val(url);
			$("#orders").val(orders);
			$("#extend").val(extend);
			$("#sub_button").val("update");
			$("#action").attr("action","<%=path%>/menu/updateMenu.do");
		}
		function del(id){
			$("#id").val(id);
			$("#action").attr("action","<%=path%>/menu/delMenu.do");
			$("#action").submit();
		}
	</script>
</head>
<body>
<form id="action"  action="<%=path%>/menu/addMenu.do">
	menu：
	parent:
	<select name="f_id" id="f_id">
		<option value="0"></option>
		<c:forEach items="${res.list }" var="flist">
		<option value="${flist.id}">${flist.name }</option>
		</c:forEach>
	</select>
	name:<input type="text" name="name" id="name">
	link:<input type="text" name="url" id="url">
	host:<input type="text" name="extend" id="extend">
	orders:<input type="text" name="orders" id="orders" value="0" maxlength="10" onkeyup="value=value.replace(/[^\d\.]/g,'')">
	<input type="hidden" name="id" id="id">
	<input type="submit" value="add" id="sub_button">
	<p/>
	<table border="1" width="90%">
	  <tr>
		  <td>parent</td>	
		  <td>name</td>	
		  <td>url</td>	
		  <td>host</td>
		  <td>orders</td>
		  <td>op</td>
	  </tr>
	<c:forEach items="${res.list }" var="list">
	  <tr>
		  <td>/${list.f_name}</td>
		  <td>${list.name}</td>	
		  <td>${list.url}</td>	
		  <td>${list.extend }</td>
		  <td>${list.orders}</td>
		  <td><a href="javascript:edit(${list.id},${list.f_id},'${list.name }','${list.url }',${list.orders },'${list.extend }')">edit</a>&nbsp;&nbsp;&nbsp;
		  	<a href="javascript:del(${list.id })">delete</a></td>
		<c:forEach items="${list.childen }" var="list">
		<tr>
		  <td>/${list.f_name}</td>	
		  <td>${list.name}</td>	
		  <td>${list.url}</td>	
		  <td>${list.extend }</td>
		  <td>${list.orders}</td>
		  <td><a href="javascript:edit(${list.id},${list.f_id},'${list.name }','${list.url }',${list.orders },'${list.extend }')">edit</a>&nbsp;&nbsp;&nbsp;
		  	<a href="javascript:del(${list.id })">delete</a></td>
		</tr>  
		</c:forEach>
	  </tr>
	</c:forEach>
	</table>
</form>

</body>
</html>