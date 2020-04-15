<#import "lib/utils.ftl" as u>

<@u.page>
    <p>Current user: ${user.name} (<a href="/logout/">logout</a>)</p>
    <h3>Customers:</h3>
    <ul>
        <#list customers as customer>
            <li>
                <a href="/app/customer/${customer.id}/">${customer.name} - ${customer.email}</a>
                <a href="/app/customer/${customer.id}/delete/">(X)</a>
            </li>
        </#list>
    </ul>

    <h3>New Customer:</h3>
    <form method="post" action="/app/customer/">
        Name: <input type="text" name="name" required><br>
        Email: <input type="text" name="email" required><br>
        <input type="submit">
    </form>
</@u.page>