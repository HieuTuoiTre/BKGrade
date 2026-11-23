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

    if (val >= 8.5) { s4 = 4.0; letter = 'A'; }
    else if (val >= 8.0) { s4 = 3.5; letter = 'B+'; }
    else if (val >= 7.0) { s4 = 3.0; letter = 'B'; }
    else if (val >= 6.5) { s4 = 2.5; letter = 'C+'; }
    else if (val >= 5.5) { s4 = 2.0; letter = 'C'; }
    else if (val >= 5.0) { s4 = 1.5; letter = 'D+'; }
    else if (val >= 4.0) { s4 = 1.0; letter = 'D'; }
    else { s4 = 0.0; letter = 'F'; }

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
