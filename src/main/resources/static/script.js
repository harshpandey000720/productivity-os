/**
 * Productivity OS - Central Frontend Controller
 */
const API_BASE_URL = 'http://localhost:8080';
const jwt = localStorage.getItem('jwtToken');
// --- 1. Theme Configuration ---
function initTheme() {
    const savedTheme = localStorage.getItem('theme');
    const toggleBtns = document.querySelectorAll('.theme-toggle');
    if (savedTheme === 'dark') {
        document.body.classList.add('dark-mode');
        toggleBtns.forEach(b => b.textContent = '☀️');
    }
    toggleBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            document.body.classList.toggle('dark-mode');
            const isDark = document.body.classList.contains('dark-mode');
            localStorage.setItem('theme', isDark ? 'dark' : 'light');
            toggleBtns.forEach(b => b.textContent = isDark ? '☀️' : '🌙');
        });
    });
}
initTheme();
// ==========================================
// AUTHENTICATION LOGIC (Runs on index.html)
// ==========================================
const loginCard = document.getElementById('loginCard');
const registerCard = document.getElementById('registerCard');
if (loginCard && registerCard) {
    // Reverse Auth Guard: If already authenticated, skip to dashboard
    if (jwt) {
        window.location.href = "home.html";
    }
    const showRegister = document.getElementById('showRegister');
    const showLogin = document.getElementById('showLogin');
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const regPasswordInput = document.getElementById('regPassword');
    const toggleRegPassword = document.getElementById('toggleRegPassword');
    const strengthBar = document.getElementById('strengthBar');
    function clearAuthMessages() {
        document.getElementById('loginMessage').className = 'msg hidden';
        document.getElementById('regMessage').className = 'msg hidden';
    }
    showRegister.addEventListener('click', (e) => {
        e.preventDefault();
        clearAuthMessages();
        loginCard.classList.add('hidden');
        registerCard.classList.remove('hidden');
    });
    showLogin.addEventListener('click', (e) => {
        e.preventDefault();
        clearAuthMessages();
        registerCard.classList.add('hidden');
        loginCard.classList.remove('hidden');
    });
    document.querySelectorAll('input').forEach(input => {
        input.addEventListener('input', clearAuthMessages);
    });
    // Password Eye Toggle
    toggleRegPassword.addEventListener('click', () => {
        const isPassword = regPasswordInput.getAttribute('type') === 'password';
        regPasswordInput.setAttribute('type', isPassword ? 'text' : 'password');
        toggleRegPassword.textContent = isPassword ? '🙈' : '👁️';
    });
    // Password Strength Evaluator
    const hasUpper = /[A-Z]/;
    const hasLower = /[a-z]/;
    const hasNumber = /[0-9]/;
    const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/;
    regPasswordInput.addEventListener('input', (e) => {
        const val = e.target.value;
        let strength = 0;
        if (val.length >= 8) strength += 25;
        if (hasUpper.test(val) && hasLower.test(val)) strength += 25;
        if (hasNumber.test(val)) strength += 25;
        if (hasSpecial.test(val)) strength += 25;
        strengthBar.style.width = strength + '%';
        if (strength <= 25) strengthBar.style.backgroundColor = '#ef4444';
        else if (strength <= 75) strengthBar.style.backgroundColor = '#f59e0b';
        else strengthBar.style.backgroundColor = '#10b981';
    });
    function showAuthMessage(elementId, text, type) {
        const el = document.getElementById(elementId);
        el.textContent = text;
        el.className = `msg ${type}`;
    }
    // Register Handler
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('regName').value.trim();
        const email = document.getElementById('regEmail').value.trim();
        const password = regPasswordInput.value;
        const regBtn = document.getElementById('regBtn');
        if (!email.includes('@')) {
            return showAuthMessage('regMessage', "Please enter a valid email address.", 'error');
        }
        if (password.length < 8 || !hasUpper.test(password) || !hasNumber.test(password) || !hasSpecial.test(password)) {
            return showAuthMessage('regMessage', "Password does not meet complexity requirements.", 'error');
        }
        regBtn.disabled = true;
        regBtn.textContent = "Registering...";
        try {
            const res = await fetch(`${API_BASE_URL}/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email, password })
            });
            const data = await res.json();
            if (res.ok && data.message === "Registration Successful") {
                showAuthMessage('regMessage', "Account registered! Switching to login...", 'success');
                setTimeout(() => showLogin.click(), 1600);
            } else {
                showAuthMessage('regMessage', data.message || "Registration failed.", 'error');
            }
        } catch (err) {
            showAuthMessage('regMessage', "Server connection error. Check backend.", 'error');
        } finally {
            regBtn.disabled = false;
            regBtn.textContent = "Register";
        }
    });
    // Login Handler
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('loginEmail').value.trim();
        const password = document.getElementById('loginPassword').value;
        const loginBtn = document.getElementById('loginBtn');
        loginBtn.disabled = true;
        loginBtn.textContent = "Logging in...";
        try {
            const res = await fetch(`${API_BASE_URL}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });
            const data = await res.json();
            if (res.ok && data.message && data.message !== "fail") {
                localStorage.setItem('jwtToken', data.message);
                window.location.href = "home.html";
            } else {
                showAuthMessage('loginMessage', "Invalid credentials. Please verify.", 'error');
            }
        } catch (err) {
            showAuthMessage('loginMessage', "Server connection error. Check backend.", 'error');
        } finally {
            loginBtn.disabled = false;
            loginBtn.textContent = "Log In";
        }
    });
}
// ==========================================
// DASHBOARD LOGIC (Runs on home.html)
// ==========================================
const taskList = document.getElementById('taskList');
if (taskList) {
    // In-memory cache for fast local filtering
    let allTasks = [];
    // Logout
    document.getElementById('logoutBtn').addEventListener('click', () => {
        localStorage.removeItem('jwtToken');
        window.location.href = "index.html";
    });
    // Modals
    const createModal = document.getElementById('createTaskModal');
    const editModal = document.getElementById('editTaskModal');
    document.getElementById('openCreateModalBtn').addEventListener('click', () => createModal.classList.remove('hidden'));
    document.getElementById('closeCreateModalBtn').addEventListener('click', () => createModal.classList.add('hidden'));
    document.getElementById('closeEditModalBtn').addEventListener('click', () => editModal.classList.add('hidden'));
    // Filter Listeners
    const searchInput = document.getElementById('searchInput');
    const statusFilter = document.getElementById('statusFilter');
    const priorityFilter = document.getElementById('priorityFilter');
    searchInput.addEventListener('input', applyFilters);
    statusFilter.addEventListener('change', applyFilters);
    priorityFilter.addEventListener('change', applyFilters);
    function applyFilters() {
        const query = searchInput.value.toLowerCase().trim();
        const selectedStatus = statusFilter.value;
        const selectedPriority = priorityFilter.value;
        const filtered = allTasks.filter(task => {
            const matchesSearch = (task.title || '').toLowerCase().includes(query) ||
                (task.description || '').toLowerCase().includes(query);
            const matchesStatus = selectedStatus === 'ALL' || (task.status || 'TODO') === selectedStatus;
            const matchesPriority = selectedPriority === 'ALL' || (task.priority || 'MEDIUM') === selectedPriority;
            return matchesSearch && matchesStatus && matchesPriority;
        });
        renderTasks(filtered);
    }
    function renderTasks(tasks) {
        taskList.innerHTML = "";
        if (tasks.length === 0) {
            taskList.innerHTML = `<p class="empty-state">No matching tasks found.</p>`;
            return;
        }
        tasks.forEach(task => {
            const card = document.createElement('div');
            const status = task.status || 'TODO';
            card.className = `task-card status-border-${status}`;
            card.id = `task-card-${task.id}`;
            card.dataset.task = JSON.stringify(task);
            card.innerHTML = `
                <div>
                    <div class="task-header">
                        <span class="task-title">${escapeHtml(task.title)}</span>
                        <div class="task-actions">
                            <button class="btn-icon" onclick="openEditModal(${task.id})" title="Edit Task">✏️</button>
                            <button class="btn-icon" onclick="deleteTask(${task.id})" title="Delete Task">🗑️</button>
                        </div>
                    </div>
                    <p class="task-desc">${escapeHtml(task.description || 'No description provided.')}</p>
                </div>
                <div class="task-footer">
                    <span class="badge-priority">${task.priority || 'MEDIUM'}</span>
                    <span class="task-due">Due: ${task.deadline ? task.deadline.split('T')[0] : 'None'}</span>
                </div>
            `;
            taskList.appendChild(card);
        });
    }
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    // Read: Fetch Tasks
    async function loadTasks() {
        try {
            const res = await fetch(`${API_BASE_URL}/Tasks`, {
                headers: { 'Authorization': `Bearer ${jwt}` }
            });
            if (res.status === 401 || res.status === 403) {
                localStorage.removeItem('jwtToken');
                window.location.href = "index.html";
                return;
            }
            allTasks = await res.json();
            applyFilters();
        } catch (err) {
            taskList.innerHTML = `<p class="empty-state" style="color: var(--danger)">Failed to connect to task service.</p>`;
        }
    }
    // Create: POST /Tasks
    document.getElementById('createTaskForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            title: document.getElementById('createTitle').value.trim(),
            description: document.getElementById('createDescription').value.trim(),
            priority: document.getElementById('createPriority').value,
            deadline: document.getElementById('createDeadline').value || null,
            status: 'TODO'
        };
        try {
            const res = await fetch(`${API_BASE_URL}/Tasks`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${jwt}`
                },
                body: JSON.stringify(payload)
            });
            if (res.ok) {
                createModal.classList.add('hidden');
                document.getElementById('createTaskForm').reset();
                loadTasks();
            } else {
                alert("Failed to create task. Check input constraints.");
            }
        } catch (err) {
            console.error("Task creation failed:", err);
        }
    });
    // Edit Modal Open & Form Fill
    window.openEditModal = function (id) {
        const card = document.getElementById(`task-card-${id}`);
        if (!card) return;
        const task = JSON.parse(card.dataset.task);
        document.getElementById('editTaskId').value = task.id;
        document.getElementById('editTitle').value = task.title || '';
        document.getElementById('editDescription').value = task.description || '';
        document.getElementById('editPriority').value = task.priority || 'MEDIUM';
        document.getElementById('editStatus').value = task.status || 'TODO';
        document.getElementById('editDeadline').value = task.deadline ? task.deadline.split('T')[0] : '';
        editModal.classList.remove('hidden');
    };
    // Update: PUT /Tasks/{id}
    document.getElementById('editTaskForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('editTaskId').value;
        const payload = {
            title: document.getElementById('editTitle').value.trim(),
            description: document.getElementById('editDescription').value.trim(),
            priority: document.getElementById('editPriority').value,
            status: document.getElementById('editStatus').value,
            deadline: document.getElementById('editDeadline').value || null
        };
        try {
            const res = await fetch(`${API_BASE_URL}/Tasks/${id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${jwt}`
                },
                body: JSON.stringify(payload)
            });
            if (res.ok) {
                editModal.classList.add('hidden');
                loadTasks();
            } else {
                alert("Failed to update task.");
            }
        } catch (err) {
            console.error("Task update error:", err);
        }
    });
    // Delete: DELETE /Tasks/{id}
    window.deleteTask = async function (id) {
        if (!confirm("Are you sure you want to delete this task?")) return;
        try {
            const res = await fetch(`${API_BASE_URL}/Tasks/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${jwt}` }
            });
            if (res.ok) {
                allTasks = allTasks.filter(t => t.id !== id);
                applyFilters();
            } else {
                alert("Failed to delete task.");
            }
        } catch (err) {
            console.error("Delete task error:", err);
        }
    };
    // Initial Load
    loadTasks();
}
// --- AI Assistant Client Flow (DEBUG VERSION) ---
(function() { // Wrap in an immediately invoked function to ensure it runs
    const aiChatForm = document.getElementById('aiChatForm');
    const aiChatBox = document.getElementById('aiChatBox');
    const aiPromptInput = document.getElementById('aiPromptInput');
    const toggleAiBtn = document.getElementById('toggleAiBtn');
    const aiWidget = document.querySelector('.ai-widget');

    console.log("AI Script Loaded. Found form:", !!aiChatForm);

    // Close / Minimize button logic
    if (toggleAiBtn && aiWidget) {
        // Toggle minimize state on button click
        toggleAiBtn.addEventListener('click', (e) => {
            e.stopPropagation(); // Prevents the click from triggering the parent listener below
            aiWidget.classList.toggle('minimized');
            
            // Swap the icon
            if (aiWidget.classList.contains('minimized')) {
                toggleAiBtn.textContent = '💬';
            } else {
                toggleAiBtn.textContent = '✖';
            }
        });

        // Clicking anywhere on the minimized widget expands it again
        aiWidget.addEventListener('click', () => {
            if (aiWidget.classList.contains('minimized')) {
                aiWidget.classList.remove('minimized');
                toggleAiBtn.textContent = '✖';
            }
        });
    }

    if (aiChatForm) {
        aiChatForm.addEventListener('submit', async (e) => {
            e.preventDefault(); 
            console.log("1. Form submitted. Default prevented.");
            
            const prompt = aiPromptInput.value.trim();
            console.log("2. Prompt captured:", prompt);
            
            if (!prompt) return;

            // 1. Show user message
            appendAiMessage(prompt, 'user');
            aiPromptInput.value = '';
            console.log("3. User message appended to chat window.");

            // 2. Fetch from Spring Boot
            try {
                console.log("4. Sending POST request to backend...");
                const res = await fetch(`${API_BASE_URL}/api/ai/chat`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${jwt}` 
                    },
                    body: JSON.stringify({ prompt: prompt })
                });

                console.log("5. Received response. HTTP Status:", res.status);

                if (res.ok) {
                    const data = await res.json();
                    console.log("6. Parsed JSON from backend:", data);
                    appendAiMessage(data.message || "[Empty Reply from Backend]", 'bot');
                } else {
                    appendAiMessage(`Backend rejected request: Status ${res.status}`, 'bot');
                }
            } catch (err) {
                console.error("7. Fetch Error:", err);
                appendAiMessage("Network connection failed. Check console.", 'bot');
            }
        });

        function appendAiMessage(text, role) {
            console.log(`-> Appending [${role}] message:`, text);
            const div = document.createElement('div');
            div.className = `ai-msg ${role}`;
            div.textContent = text;
            aiChatBox.appendChild(div);
            aiChatBox.scrollTop = aiChatBox.scrollHeight;
        }
    }
})();