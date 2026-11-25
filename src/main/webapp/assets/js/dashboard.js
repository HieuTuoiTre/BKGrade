/**
 * DUT Grade Calculator - Dashboard Interactions & GPA Simulator
 */

function openEditModalFromBtn(btn) {
    if (!btn || !btn.dataset) return;
    var d = btn.dataset;
    openEditModal(d.id, d.name, d.sem, d.credits, d.formula, d.bt, d.bv, d.ck, d.da, d.gk, d.qt, d.th, d.s10, d.s4, d.sletter);
}

function openEditModal(id, name, sem, credits, formula, bt, bv, ck, da, gk, qt, th, s10, s4, sLetter) {
    document.getElementById('editGradeId').value = id;
    document.getElementById('editCourseName').innerText = name || '';
    document.getElementById('editSemester').innerText = sem || '';
    document.getElementById('editCreditsBadge').innerText = (credits || '0') + ' Tín chỉ';
    document.getElementById('editFormula').value = (formula === 'null' || !formula) ? '' : formula;

    document.getElementById('editScoreBt').value = (bt === 'null' || !bt) ? '' : bt;
    document.getElementById('editScoreBv').value = (bv === 'null' || !bv) ? '' : bv;
    document.getElementById('editScoreCk').value = (ck === 'null' || !ck) ? '' : ck;
    document.getElementById('editScoreDa').value = (da === 'null' || !da) ? '' : da;
    document.getElementById('editScoreGk').value = (gk === 'null' || !gk) ? '' : gk;
    document.getElementById('editScoreQt').value = (qt === 'null' || !qt) ? '' : qt;
    document.getElementById('editScoreTh').value = (th === 'null' || !th) ? '' : th;

    document.getElementById('editScore10').value = (s10 === 'null' || !s10) ? '' : s10;
    document.getElementById('editScore4').value = (s4 === 'null' || !s4) ? '' : s4;
    document.getElementById('editScoreLetter').value = (sLetter === 'null' || !sLetter) ? '' : sLetter;

    var modalElement = document.getElementById('editGradeModal');
    if (modalElement) {
        var modal = new bootstrap.Modal(modalElement);
        modal.show();
        // Cập nhật bảng gợi ý dự đoán điểm CK cần đạt
        if (window.DutSimulator && typeof window.DutSimulator.updateModalPredictionTable === 'function') {
            window.DutSimulator.updateModalPredictionTable('edit');
        }
    }
}

function calculateSimulator(prefix) {
    var formulaInput = document.getElementById(prefix + 'Formula');
    if (!formulaInput) return;
    var formula = formulaInput.value.trim();
    if (!formula) return;

    var scores = {
        'BT': parseNum(document.getElementById(prefix + 'ScoreBt').value),
        'BV': parseNum(document.getElementById(prefix + 'ScoreBv').value),
        'CK': parseNum(document.getElementById(prefix + 'ScoreCk').value),
        'DA': parseNum(document.getElementById(prefix + 'ScoreDa').value),
        'GK': parseNum(document.getElementById(prefix + 'ScoreGk').value),
        'QT': parseNum(document.getElementById(prefix + 'ScoreQt').value),
        'TH': parseNum(document.getElementById(prefix + 'ScoreTh').value)
    };

    var regex = /\[([A-Za-z0-9_]+)\]\s*\*\s*([0-9.]+)/g;
    var match;
    var total = 0;
    var totalWeight = 0;
    var complete = true;

    while ((match = regex.exec(formula)) !== null) {
        var comp = match[1].toUpperCase();
        var weight = parseFloat(match[2]);
        totalWeight += weight;

        if (scores[comp] === null || isNaN(scores[comp])) {
            complete = false;
        } else {
            total += scores[comp] * weight;
        }
    }

    if (complete && totalWeight > 0) {
        var s10 = Math.round(total * 10) / 10;
        document.getElementById(prefix + 'Score10').value = s10;
        syncFromScore10(prefix);
    }

    if (window.DutSimulator && typeof window.DutSimulator.updateModalPredictionTable === 'function') {
        window.DutSimulator.updateModalPredictionTable(prefix);
    }
}

function syncFromScore10(prefix) {
    var s10Input = document.getElementById(prefix + 'Score10');
    if (!s10Input) return;
    var val = parseNum(s10Input.value);
    var s4Input = document.getElementById(prefix + 'Score4');
    var sLetterInput = document.getElementById(prefix + 'ScoreLetter');

    if (val === null) {
        if (s4Input) s4Input.value = '';
        if (sLetterInput) sLetterInput.value = '';
        return;
    }

    var s4 = 0.0;
    var letter = 'F';

    if (window.DutSimulator && typeof window.DutSimulator.score10ToScore4 === 'function') {
        s4 = window.DutSimulator.score10ToScore4(val);
        letter = window.DutSimulator.score10ToLetter(val);
    } else {
        if (val >= 8.5) { s4 = 4.0; letter = 'A'; }
        else if (val >= 8.0) { s4 = 3.5; letter = 'B+'; }
        else if (val >= 7.0) { s4 = 3.0; letter = 'B'; }
        else if (val >= 6.5) { s4 = 2.5; letter = 'C+'; }
        else if (val >= 5.5) { s4 = 2.0; letter = 'C'; }
        else if (val >= 5.0) { s4 = 1.5; letter = 'D+'; }
        else if (val >= 4.0) { s4 = 1.0; letter = 'D'; }
        else { s4 = 0.0; letter = 'F'; }
    }

    if (s4Input) s4Input.value = s4.toFixed(1);
    if (sLetterInput) sLetterInput.value = letter;
}

function setFormulaPreset(prefix, formulaStr) {
    var formulaInput = document.getElementById(prefix + 'Formula');
    if (formulaInput) {
        formulaInput.value = formulaStr;
        calculateSimulator(prefix);
    }
}

function parseNum(val) {
    if (val === undefined || val === null || val.trim() === '') return null;
    var n = parseFloat(val);
    return isNaN(n) ? null : n;
}

/* ==========================================================================
   CHẾ ĐỘ CHỌN & XÓA HÀNG LOẠT MÔN HỌC (BULK DELETE)
   ========================================================================== */

var bulkSelectActive = false;

function toggleBulkSelectMode(enable) {
    bulkSelectActive = !!enable;

    var cols = document.querySelectorAll('.bulk-select-col');
    cols.forEach(function (col) {
        col.classList.toggle('d-none', !bulkSelectActive);
    });

    var startBtn = document.getElementById('btnStartBulkSelect');
    var actionGroup = document.getElementById('bulkActionGroup');
    if (startBtn) {
        startBtn.classList.toggle('d-none', bulkSelectActive);
    }
    if (actionGroup) {
        actionGroup.classList.toggle('d-none', !bulkSelectActive);
        actionGroup.classList.toggle('d-flex', bulkSelectActive);
    }

    if (!bulkSelectActive) {
        var masterCb = document.getElementById('selectAllGrades');
        if (masterCb) {
            masterCb.checked = false;
            masterCb.indeterminate = false;
        }
        document.querySelectorAll('.grade-select-checkbox').forEach(function (cb) {
            cb.checked = false;
        });
    }

    updateBulkSelectionUI();
}

function toggleSelectAllGrades(masterCheckbox) {
    var checked = !!(masterCheckbox && masterCheckbox.checked);
    document.querySelectorAll('.grade-select-checkbox').forEach(function (cb) {
        cb.checked = checked;
    });
    updateBulkSelectionUI();
}

function handleGradeRowClick(event, row) {
    if (!bulkSelectActive || !row) return;
    if (event.target && event.target.closest('button, a, form, input, label')) {
        return;
    }
    var cb = row.querySelector('.grade-select-checkbox');
    if (cb) {
        cb.checked = !cb.checked;
        updateBulkSelectionUI();
    }
}

function updateBulkSelectionUI() {
    var checkboxes = document.querySelectorAll('.grade-select-checkbox');
    var selectedCount = 0;
    var totalCount = checkboxes.length;

    checkboxes.forEach(function (cb) {
        if (cb.checked) {
            selectedCount++;
        }
        var row = cb.closest('.grade-row');
        if (row) {
            row.classList.toggle('table-danger', bulkSelectActive && cb.checked);
        }
    });

    var countSpan = document.getElementById('bulkSelectedCount');
    if (countSpan) {
        countSpan.textContent = selectedCount;
    }

    var deleteBtn = document.getElementById('btnOpenBulkDeleteModal');
    if (deleteBtn) {
        deleteBtn.disabled = (selectedCount === 0);
    }

    var masterCb = document.getElementById('selectAllGrades');
    if (masterCb) {
        masterCb.checked = (totalCount > 0 && selectedCount === totalCount);
        masterCb.indeterminate = (selectedCount > 0 && selectedCount < totalCount);
    }
}

function openBulkDeleteModal() {
    var checkedBoxes = document.querySelectorAll('.grade-select-checkbox:checked');
    if (checkedBoxes.length === 0) return;

    var countEl = document.getElementById('bulkConfirmCount');
    if (countEl) {
        countEl.textContent = checkedBoxes.length;
    }

    var hiddenContainer = document.getElementById('bulkDeleteHiddenInputs');
    if (hiddenContainer) {
        hiddenContainer.innerHTML = '';
        checkedBoxes.forEach(function (cb) {
            var input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'gradeIds';
            input.value = cb.value;
            hiddenContainer.appendChild(input);
        });
    }

    var modalEl = document.getElementById('bulkDeleteConfirmModal');
    if (modalEl) {
        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }
}
