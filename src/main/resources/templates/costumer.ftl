<h1>MrBill app</h1>
<p>Current user: ${user.name} (<a href="/logout/">Logout</a>)</p>

<h3>Invoices for ${costumer.name}</h3>
<ul>
    <#list invoices as invoice>
    <li>${invoice.date} ${invoice.amount}</li>
</#list>
</ul>
