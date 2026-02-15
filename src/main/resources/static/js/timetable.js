const DAYS = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"];

function fetchTimetable() {
    const standard = document.getElementById("standard").value;
    const division = document.getElementById("division").value;

    if (!standard || !division) {
        alert("Please enter both Standard and Division");
        return;
    }

    const url = `/api/timetable?standard=${standard}&division=${division}`;

    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to fetch timetable");
            }
            return response.json();
        })
        .then(data => renderTable(data))
        .catch(error => {
            console.error(error);
            alert(error.message);
        });
}

function renderTable(data) {
    const tbody = document.querySelector("#timetable tbody");
    tbody.innerHTML = "";

    // 🔹 Collect unique periods
    const periods = [...new Set(data.map(e => e.slot.periodNo))]
        .sort((a, b) => a - b);

    // 🔹 Build lookup: period -> day -> entry
    const timetableMap = {};

    data.forEach(entry => {
        const period = entry.slot.periodNo;
        const day = entry.slot.day;

        if (!timetableMap[period]) {
            timetableMap[period] = {};
        }

        timetableMap[period][day] = entry;
    });

    // 🔹 Render rows
    periods.forEach(periodNo => {
        const row = document.createElement("tr");

        // Period column
        const periodCell = document.createElement("td");
        periodCell.innerHTML = `<strong>${periodNo}</strong>`;
        row.appendChild(periodCell);

        // Day columns
        DAYS.forEach(day => {
            const cell = document.createElement("td");

            const entry = timetableMap[periodNo]?.[day];

            if (entry) {
                cell.innerHTML = `
                    ${entry.subject}<br>
                    <small>${entry.teacher}</small>
                `;
            } else {
                cell.textContent = "—";
            }

            row.appendChild(cell);
        });

        tbody.appendChild(row);
    });
}
function assignTeacher() {

    const req = {
        standard: document.getElementById("a_standard").value,
        division: document.getElementById("a_division").value,
        day: document.getElementById("a_day").value,
        period: parseInt(document.getElementById("a_period").value),
        subject: document.getElementById("a_subject").value,
        teacher: document.getElementById("a_teacher").value
    };

    fetch("/api/timetable/assign", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req)
    })
    .then(response => {

        if (!response.ok) {
            throw new Error("Teacher assignment Failed");
        }

        return response.json();
    })
    .then(data => {
        alert("Teacher Assigned");
    })
    .catch(err => {
        alert(err.message);
        console.error("Assignment error:", err);
    });
}

function swapTeachers() {

    const req = {
        standard: document.getElementById("s_standard").value,
        division: document.getElementById("s_division").value,
        day1: document.getElementById("s_day1").value,
        periodNo1: parseInt(document.getElementById("s_period1").value),
        day2: document.getElementById("s_day2").value,
        periodNo2: parseInt(document.getElementById("s_period2").value)
    };

    fetch("/api/timetable/swap", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req)
    })
    .then(response => {

            if (!response.ok) {
                throw new Error("Swap Failed");
            }

            return response.json();
        })
        .then(data => {
            alert("Swapping Successful");
        })
        .catch(err => {
            alert(err.message);
            console.error("Swap error:", err);
        });
}

