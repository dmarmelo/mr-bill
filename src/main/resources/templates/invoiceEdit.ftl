<#import "lib/utils.ftl" as u>

<@u.page>
    <p>Current user: ${user.name} (<a href="/logout/">Logout</a>)</p>

    <h3>Customer: ${customer.name}</h3>

    <h3>Edit Invoice:</h3>
    <form method="post" action="/app/invoice/${invoice.id}/edit/">
        Date: <input type="date" name="date" required value="${invoice.date}"><br>
        Amount: <input type="number" step="0.01" name="amount" required value="${invoice.amount?replace(",", ".")}"><br>
        <input type="submit">
    </form>
</@u.page>
