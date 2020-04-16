<#import "lib/utils.ftl" as u>

<@u.page>
    <p>Current user: ${user.name} (<a href="/logout/">logout</a>)</p>

    <h3>Invoices for ${customer.name}</h3>
    <ul>
        <#list invoices as invoice>
        <li>
            ${invoice.date} - ${invoice.amount}
            <#if invoice.complete == 0>
                <a href="/app/invoice/${invoice.id}/edit/">(Edit)</a>
                <a href="/app/invoice/${invoice.id}/complete/">(Complete)</a>
            <#else>
                (Completed)
            </#if>
            <a href="/app/invoice/${invoice.id}/delete/">(X)</a>
        </li>
    </#list>
    </ul>

    <h3>New Invoice:</h3>
    <form method="post" action="/app/customer/${customer.id}/invoice/">
        Date: <input type="date" name="date" required><br>
        Amount: <input type="number" step="0.01" name="amount" required><br>
        <input type="submit">
    </form>
</@u.page>
