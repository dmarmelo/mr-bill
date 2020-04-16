<#import "lib/utils.ftl" as u>

<@u.page>
    <p>Current user: ${user.name} (<a href="/logout/">logout</a>)</p>

    <h3>Edit Customer:</h3>
    <form method="post" action="/app/customer/${customer.id}/edit/">
        Name: <input type="text" name="name" required value="${customer.name}"><br>
        Email: <input type="text" name="email" required value="${customer.email}"><br>
        <input type="submit">
    </form>
</@u.page>
