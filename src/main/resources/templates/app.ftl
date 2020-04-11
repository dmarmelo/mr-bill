<h1>MrBill app</h1>
<p>Current user: ${user.name} (<a href="/logout/">Logout</a>)</p>
<h3>Costumers:</h3>
<ul>
    <#list costumers as costumer>
        <li>
            <a href="/app/costumer/${costumer.id}/">${costumer.name} - ${costumer.email}</a>
            <a href="/app/costumer/${costumer.id}/delete/">(X)</a>
        </li>
    </#list>
</ul>

<h3>New Costumer:</h3>
<form method="post" action="/app/costumer/">
    Name: <input type="text" name="name"><br>
    Email: <input type="text" name="email"><br>
    <input type="submit">
</form>

<#-- https://www.vogella.com/tutorials/FreeMarker/article.html -->