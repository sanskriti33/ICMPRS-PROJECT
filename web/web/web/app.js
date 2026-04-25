const previewText = document.getElementById("previewText");
const complaintId = document.getElementById("complaintId");
const complaintDescription = document.getElementById("complaintDescription");
const complaintTableBody = document.getElementById("complaintTableBody");
const storageName = document.getElementById("storageName");
const searchResult = document.getElementById("searchResult");

const totalCount = document.getElementById("totalCount");
const highCount = document.getElementById("highCount");
const openCount = document.getElementById("openCount");
const resolvedCount = document.getElementById("resolvedCount");

complaintDescription.addEventListener("input", updatePreview);
document.getElementById("submitComplaint").addEventListener("click", createComplaint);
document.getElementById("clearForm").addEventListener("click", clearForm);
document.getElementById("refreshComplaints").addEventListener("click", loadComplaints);
document.getElementById("searchComplaint").addEventListener("click", searchComplaint);
document.getElementById("updateStatus").addEventListener("click", updateStatus);
document.getElementById("deleteComplaint").addEventListener("click", deleteComplaint);

loadComplaints();
updatePreview();

async function loadComplaints() {
  const response = await fetch("/api/complaints");
  const data = await response.json();
  storageName.textContent = data.storage || "Unknown";
  renderTable(data.complaints || []);
  updateStats(data.complaints || []);
}

function renderTable(complaints) {
  if (!complaints.length) {
    complaintTableBody.innerHTML = `<tr><td colspan="8" class="empty-cell">No complaints yet.</td></tr>`;
    return;
  }

  complaintTableBody.innerHTML = complaints.map((complaint) => `
    <tr class="${complaint.priorityLevel === "HIGH" ? "high-priority" : ""}">
      <td>${complaint.id}</td>
      <td>${escapeHtml(complaint.description)}</td>
      <td>${complaint.priority}</td>
      <td>${complaint.priorityLevel}</td>
      <td>${complaint.department}</td>
      <td>${complaint.status}</td>
      <td>${complaint.highFlag}</td>
      <td>${complaint.createdAt}</td>
    </tr>
  `).join("");
}

function updateStats(complaints) {
  totalCount.textContent = complaints.length;
  highCount.textContent = complaints.filter(item => item.highFlag === 1).length;
  openCount.textContent = complaints.filter(item => item.status.toLowerCase() === "open").length;
  resolvedCount.textContent = complaints.filter(item => ["resolved", "closed"].includes(item.status.toLowerCase())).length;
}

async function createComplaint() {
  const payload = {
    id: Number(complaintId.value),
    description: complaintDescription.value
  };

  const response = await fetch("/api/complaints", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });

  const data = await response.json();
  if (!response.ok) {
    alert(data.message || "Unable to submit complaint.");
    return;
  }

  alert("Complaint submitted successfully.");
  clearForm();
  loadComplaints();
}

async function searchComplaint() {
  const id = document.getElementById("searchId").value;
  if (!id) {
    searchResult.textContent = "Enter a complaint ID first.";
    return;
  }

  const response = await fetch(`/api/complaints/${id}`);
  const data = await response.json();
  if (!response.ok) {
    searchResult.textContent = data.message || "Complaint not found.";
    return;
  }

  const complaint = data.complaint;
  searchResult.innerHTML = `
    <strong>ID:</strong> ${complaint.id}<br>
    <strong>Description:</strong> ${escapeHtml(complaint.description)}<br>
    <strong>Priority:</strong> ${complaint.priority} (${complaint.priorityLevel})<br>
    <strong>Department:</strong> ${complaint.department}<br>
    <strong>Status:</strong> ${complaint.status}<br>
    <strong>Created:</strong> ${complaint.createdAt}
  `;
}

async function updateStatus() {
  const id = document.getElementById("statusId").value;
  const status = document.getElementById("statusValue").value;

  if (!id) {
    alert("Enter complaint ID first.");
    return;
  }

  const response = await fetch(`/api/complaints/${id}/status`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status })
  });

  const data = await response.json();
  if (!response.ok) {
    alert(data.message || "Unable to update status.");
    return;
  }

  alert("Status updated successfully.");
  loadComplaints();
}

async function deleteComplaint() {
  const id = document.getElementById("deleteId").value;
  if (!id) {
    alert("Enter complaint ID first.");
    return;
  }

  const response = await fetch(`/api/complaints/${id}`, { method: "DELETE" });
  const data = await response.json();
  if (!response.ok) {
    alert(data.message || "Unable to delete complaint.");
    return;
  }

  alert("Complaint deleted successfully.");
  loadComplaints();
}

function clearForm() {
  complaintId.value = "";
  complaintDescription.value = "";
  updatePreview();
}

function updatePreview() {
  const text = complaintDescription.value.toLowerCase();
  let sentiment = 2;
  let impact = 2;

  const negativeWords = ["angry", "worst", "bad", "terrible", "hate", "frustrated", "issue", "problem", "poor", "disappointed", "complaint", "unhappy"];
  const criticalWords = ["crash", "failed", "error", "not working", "down", "broken", "urgent", "blocked", "loss", "security", "fraud", "critical"];

  const negativeMatches = negativeWords.filter(word => text.includes(word)).length;
  const criticalMatches = criticalWords.filter(word => text.includes(word)).length;

  if (negativeMatches >= 3) sentiment = 5;
  else if (negativeMatches >= 1) sentiment = 4;

  if (criticalMatches >= 2) impact = 5;
  else if (criticalMatches === 1) impact = 4;

  const priority = sentiment + impact;
  const level = priority >= 9 ? "HIGH" : priority >= 6 ? "MEDIUM" : "LOW";
  const department = detectDepartment(text);

  previewText.textContent = `Priority: ${priority} | Level: ${level} | Department: ${department}`;
}

function detectDepartment(text) {
  if (hasAny(text, ["payment", "refund", "billing", "invoice", "money", "transaction"])) return "Finance";
  if (hasAny(text, ["login", "password", "account", "app", "website", "server", "software"])) return "IT Support";
  if (hasAny(text, ["delivery", "order", "package", "shipment", "courier", "late"])) return "Logistics";
  if (hasAny(text, ["product", "quality", "damaged", "defective", "broken", "replacement"])) return "Product Team";
  return "General Support";
}

function hasAny(text, list) {
  return list.some(item => text.includes(item));
}

function escapeHtml(value) {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}
