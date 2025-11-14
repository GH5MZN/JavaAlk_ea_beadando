// Forex Platform JavaScript Application

let exchangeChart = null;

// SOAP - Devizaárfolyam lekérése
async function fetchExchangeRates() {
    const currency = document.getElementById('currency').value;
    const dateFrom = document.getElementById('dateFrom').value;
    const dateTo = document.getElementById('dateTo').value;

    if (!currency || !dateFrom || !dateTo) {
        alert('Kérjük, töltse ki az összes mezőt!');
        return;
    }

    // Megjelenítjük a loading üzenetet
    const resultsDiv = document.getElementById('soap-results');
    resultsDiv.innerHTML = '<p style="text-align: center; color: #00d4ff;"><strong>⏳ Adatok lekérése az MNB-ből...</strong></p>';
    resultsDiv.style.display = 'block';

    try {
        console.log(`SOAP kérés: ${currency}, ${dateFrom} - ${dateTo}`);
        const response = await fetch(`/api/soap/exchange-rates?currency=${currency}&dateFrom=${dateFrom}&dateTo=${dateTo}`);

        if (!response.ok) {
            throw new Error(`HTTP hiba! Status: ${response.status}`);
        }

        const data = await response.json();
        console.log('SOAP válasz:', data);

        if (data.success && Object.keys(data.data).length > 0) {
            displayExchangeRates(data);
            createChart(data);
            console.log(`✅ Sikeresen lekérve ${Object.keys(data.data).length} árfolyamadatok`);
        } else {
            resultsDiv.innerHTML = `<p style="color: #ff6b6b;"><strong>⚠️ Nincs elérhető adat</strong> a(z) <strong>${currency}</strong> devizához a <strong>${dateFrom}</strong> - <strong>${dateTo}</strong> időtartamban.</p>`;
            console.warn('Nincs adat:', data);
        }
    } catch (error) {
        console.error('SOAP hiba:', error);
        resultsDiv.innerHTML = `<p style="color: #ff6b6b;"><strong>❌ Hiba az adatok lekérése során:</strong><br/>${error.message}</p>`;
    }
}

function displayExchangeRates(data) {
    const tableBody = document.getElementById('soap-table-body');
    const resultsDiv = document.getElementById('soap-results');

    tableBody.innerHTML = '';

    // Dátumok szerinti rendezés
    const sortedDates = Object.keys(data.data).sort().reverse();

    sortedDates.forEach(date => {
        const rate = data.data[date];
        const row = document.createElement('tr');
        row.style.borderBottom = '1px solid rgba(212, 212, 212, 0.3)';
        row.innerHTML = `
            <td style="padding: 0.75em; border: 1px solid rgba(212, 212, 212, 0.3);">${date}</td>
            <td style="padding: 0.75em; border: 1px solid rgba(212, 212, 212, 0.3); color: #00d4ff;"><strong>${parseFloat(rate).toFixed(2)}</strong></td>
        `;
        tableBody.appendChild(row);
    });

    // Összefoglaló információk
    const rates = sortedDates.map(date => parseFloat(data.data[date]));
    const minRate = Math.min(...rates);
    const maxRate = Math.max(...rates);
    const avgRate = (rates.reduce((a, b) => a + b, 0) / rates.length).toFixed(2);

    const statsHtml = `
        <div style="background: rgba(0, 212, 255, 0.1); padding: 1em; border-radius: 4px; margin-bottom: 1em;">
            <h4 style="margin-top: 0; color: #00d4ff;">📊 Statisztika - ${data.currency}</h4>
            <p><strong>Adatpontok:</strong> ${rates.length}</p>
            <p><strong>Legmagasabb:</strong> <span style="color: #90EE90;">${maxRate.toFixed(2)}</span></p>
            <p><strong>Legalacsonyabb:</strong> <span style="color: #FF6B6B;">${minRate.toFixed(2)}</span></p>
            <p><strong>Átlagár:</strong> ${avgRate}</p>
        </div>
    `;

    resultsDiv.innerHTML = `<h3>Árfolyamadatok - ${data.currency}</h3>${statsHtml}<div style="overflow-x: auto; margin: 1em 0;"><table style="width: 100%; border-collapse: collapse;">
        <thead>
            <tr style="background: rgba(0, 212, 255, 0.2);">
                <th style="padding: 0.75em; text-align: left; border: 1px solid rgba(212, 212, 212, 0.3);">Dátum</th>
                <th style="padding: 0.75em; text-align: left; border: 1px solid rgba(212, 212, 212, 0.3);">Árfolyam (HUF)</th>
            </tr>
        </thead>
        <tbody id="soap-table-body">
        </tbody>
    </table></div><div style="position: relative; height: 400px; margin: 2em 0;">
        <canvas id="exchangeChart"></canvas>
    </div>`;

    resultsDiv.style.display = 'block';

    // Táblázat újra feltöltése
    const newTableBody = document.getElementById('soap-table-body');
    sortedDates.forEach(date => {
        const rate = data.data[date];
        const row = document.createElement('tr');
        row.style.borderBottom = '1px solid rgba(212, 212, 212, 0.3)';
        row.innerHTML = `
            <td style="padding: 0.75em; border: 1px solid rgba(212, 212, 212, 0.3);">${date}</td>
            <td style="padding: 0.75em; border: 1px solid rgba(212, 212, 212, 0.3); color: #00d4ff;"><strong>${parseFloat(rate).toFixed(2)}</strong></td>
        `;
        newTableBody.appendChild(row);
    });
}

function createChart(data) {
    const ctx = document.getElementById('exchangeChart');
    if (!ctx) return;

    // Előző chart megsemmisítése ha létezik
    if (exchangeChart) {
        exchangeChart.destroy();
    }

    // Adatok előkészítése
    const dates = Object.keys(data.data).sort();
    const rates = dates.map(date => parseFloat(data.data[date]));

    exchangeChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: dates,
            datasets: [{
                label: `${data.currency} árfolyam (HUF)`,
                data: rates,
                borderColor: '#00d4ff',
                backgroundColor: 'rgba(0, 212, 255, 0.1)',
                borderWidth: 2,
                fill: true,
                tension: 0.4,
                pointBackgroundColor: '#00d4ff',
                pointBorderColor: '#fff',
                pointBorderWidth: 2,
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: {
                        color: '#ccc',
                        font: {
                            size: 14
                        }
                    }
                }
            },
            scales: {
                y: {
                    ticks: {
                        color: '#ccc'
                    },
                    grid: {
                        color: 'rgba(212, 212, 212, 0.1)'
                    }
                },
                x: {
                    ticks: {
                        color: '#ccc'
                    },
                    grid: {
                        color: 'rgba(212, 212, 212, 0.1)'
                    }
                }
            }
        }
    });
}

// Aktuális ár lekérése
function fetchCurrentPrice() {
    const instrument = document.getElementById('aktár-instrument').value;

    if (!instrument) {
        alert('Kérjük, válasszon instrumentumot!');
        return;
    }

    // Demo adatok - valós API helyett
    const prices = {
        'EUR_USD': '1.0850',
        'GBP_USD': '1.2750',
        'USD_JPY': '149.50',
        'USD_CHF': '0.8850',
        'AUD_USD': '0.6550',
        'USD_CAD': '1.3650'
    };

    const price = prices[instrument] || '0.0000';

    document.getElementById('aktár-title').textContent = instrument;
    document.getElementById('aktár-price').textContent = price;
    document.getElementById('aktár-result').style.display = 'block';
}

// Historikus árak lekérése
function fetchHistoricalPrice() {
    const instrument = document.getElementById('histár-instrument').value;
    const granularity = document.getElementById('granularity').value;

    if (!instrument) {
        alert('Kérjük, válasszon instrumentumot!');
        return;
    }

    // Demo adatok
    const historicalData = [
        { time: '2025-01-10', open: '1.0800', high: '1.0850', low: '1.0780', close: '1.0840' },
        { time: '2025-01-09', open: '1.0820', high: '1.0860', low: '1.0790', close: '1.0800' },
        { time: '2025-01-08', open: '1.0750', high: '1.0830', low: '1.0740', close: '1.0820' },
        { time: '2025-01-07', open: '1.0780', high: '1.0850', low: '1.0760', close: '1.0750' },
        { time: '2025-01-06', open: '1.0850', high: '1.0880', low: '1.0770', close: '1.0780' },
        { time: '2025-01-05', open: '1.0900', high: '1.0920', low: '1.0840', close: '1.0850' },
        { time: '2025-01-04', open: '1.0870', high: '1.0910', low: '1.0820', close: '1.0900' },
        { time: '2025-01-03', open: '1.0880', high: '1.0950', low: '1.0860', close: '1.0870' },
        { time: '2025-01-02', open: '1.0920', high: '1.0960', low: '1.0870', close: '1.0880' },
        { time: '2025-01-01', open: '1.0950', high: '1.0980', low: '1.0910', close: '1.0920' }
    ];

    const tableBody = document.getElementById('histár-table-body');
    tableBody.innerHTML = '';

    historicalData.forEach(data => {
        const row = document.createElement('tr');
        row.style.borderBottom = '1px solid rgba(212, 212, 212, 0.3)';
        row.innerHTML = `
            <td style="padding: 0.75em; border: 1px solid rgba(212, 212, 212, 0.3);">${data.time}</td>
            <td style="padding: 0.75em; border: 1px solid rgba(212, 212, 212, 0.3);">${data.open}</td>
            <td style="padding: 0.75em; border: 1px solid rgba(212, 212, 212, 0.3); color: #90EE90;">${data.high}</td>
            <td style="padding: 0.75em; border: 1px solid rgba(212, 212, 212, 0.3); color: #FF6B6B;">${data.low}</td>
            <td style="padding: 0.75em; border: 1px solid rgba(212, 212, 212, 0.3);">${data.close}</td>
        `;
        tableBody.appendChild(row);
    });

    document.getElementById('histár-result').style.display = 'block';
}

// Pozíció megnyitása
function openPosition() {
    const instrument = document.getElementById('nyit-instrument').value;
    const amount = document.getElementById('nyit-amount').value;

    if (!instrument || !amount) {
        alert('Kérjük, töltse ki az összes mezőt!');
        return;
    }

    const type = amount > 0 ? 'LONG' : 'SHORT';
    const message = `Pozíció sikeresen megnyitva!\nInstrumentum: ${instrument}\nTípus: ${type}\nMennyiség: ${Math.abs(amount)}`;

    document.getElementById('nyit-message').textContent = message;
    document.getElementById('nyit-result').style.display = 'block';
}

// Pozíció zárása
function closePosition() {
    const tradeId = document.getElementById('zár-tradeId').value;

    if (!tradeId) {
        alert('Kérjük, adja meg a Trade ID-t!');
        return;
    }

    const message = `Pozíció #${tradeId} sikeresen zárva!\nProfit/Loss: +250.50 USD`;

    document.getElementById('zár-message').textContent = message;
    document.getElementById('zár-result').style.display = 'block';
}

// Oldal betöltésekor inicializálás
document.addEventListener('DOMContentLoaded', function() {
    // Dátummezők alapértelmezett értékei
    const today = new Date();
    const thirtyDaysAgo = new Date(today.getTime() - (30 * 24 * 60 * 60 * 1000));

    document.getElementById('dateFrom').valueAsDate = thirtyDaysAgo;
    document.getElementById('dateTo').valueAsDate = today;
});

