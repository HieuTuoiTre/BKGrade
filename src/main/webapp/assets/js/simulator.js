/**
 * DUT Grade Calculator - Grade Prediction & GPA Goal Simulator Module
 * Hỗ trợ dự đoán điểm thành phần môn học & mô phỏng mục tiêu GPA thời gian thực (Sandbox).
 */

(function () {
    'use strict';

    // Thang điểm quy đổi chuẩn DUT
    const GRADE_CONVERSION = [
        { letter: 'A', score4: 4.0, min10: 8.5, label: 'Giỏi / Xuất sắc' },
        { letter: 'B+', score4: 3.5, min10: 8.0, label: 'Khá giỏi' },
        { letter: 'B', score4: 3.0, min10: 7.0, label: 'Khá' },
        { letter: 'C+', score4: 2.5, min10: 6.5, label: 'Trung bình khá' },
        { letter: 'C', score4: 2.0, min10: 5.5, label: 'Trung bình' },
        { letter: 'D+', score4: 1.5, min10: 5.0, label: 'Trung bình yếu' },
        { letter: 'D', score4: 1.0, min10: 4.0, label: 'Yếu (Qua môn)' },
        { letter: 'F', score4: 0.0, min10: 0.0, label: 'Học lại' }
    ];

    function score10ToScore4(s10) {
        if (s10 === null || s10 === undefined || isNaN(s10)) return null;
        for (let item of GRADE_CONVERSION) {
            if (s10 >= item.min10) return item.score4;
        }
        return 0.0;
    }

    function score10ToLetter(s10) {
        if (s10 === null || s10 === undefined || isNaN(s10)) return '';
        for (let item of GRADE_CONVERSION) {
            if (s10 >= item.min10) return item.letter;
        }
        return 'F';
    }

    function letterToScore4(letter) {
        if (!letter) return 0.0;
        const found = GRADE_CONVERSION.find(item => item.letter.toUpperCase() === letter.toUpperCase().trim());
        return found ? found.score4 : 0.0;
    }

    function letterToMinScore10(letter) {
        const found = GRADE_CONVERSION.find(item => item.letter.toUpperCase() === letter.toUpperCase().trim());
        return found ? found.min10 : 0.0;
    }

    /* ==========================================================================
       PHẦN 1: DỰ ĐOÁN ĐIỂM THÀNH PHẦN MÔN HỌC (COURSE TARGET SOLVER)
       ========================================================================== */

    /**
     * Phân tích công thức và giải điểm cần đạt cho thành phần còn thiếu
     */
    function solveRequiredComponentScore(formula, scores, targetComp) {
        if (!formula || !formula.trim()) return null;

        // Bóc tách tất cả các thành phần trong công thức: [CK]*0.60
        const regex = /\[([A-Za-z0-9_]+)\]\s*\*\s*([0-9.]+)/g;
        let match;
        const weights = {};
        let totalWeight = 0;

        while ((match = regex.exec(formula)) !== null) {
            const comp = match[1].toUpperCase();
            const weight = parseFloat(match[2]);
            weights[comp] = weight;
            totalWeight += weight;
        }

        // Nếu không có targetComp được chỉ định, mặc định tìm CK, nếu không có CK thì tìm thành phần chưa có điểm
        if (!targetComp) {
            if (weights['CK']) {
                targetComp = 'CK';
            } else {
                for (let c in weights) {
                    if (scores[c] === null || scores[c] === undefined || isNaN(scores[c])) {
                        targetComp = c;
                        break;
                    }
                }
            }
        }

        if (!targetComp || !weights[targetComp]) return null;

        const targetWeight = weights[targetComp];
        let knownWeightedSum = 0;

        for (let c in weights) {
            if (c !== targetComp) {
                const s = scores[c];
                if (s !== null && s !== undefined && !isNaN(s)) {
                    knownWeightedSum += s * weights[c];
                }
            }
        }

        // Tính điểm cần đạt cho từng mốc điểm chữ A, B+, B, C+, C, D+, D
        const targets = ['A', 'B+', 'B', 'C+', 'C', 'D+', 'D'];
        const results = [];

        targets.forEach(letter => {
            const minScore10 = letterToMinScore10(letter);
            const neededScore = (minScore10 - knownWeightedSum) / targetWeight;
            const rounded = Math.round(neededScore * 10) / 10;

            let status = 'possible';
            let badgeClass = 'bg-primary';
            let displayText = '';

            if (neededScore > 10.05) {
                status = 'impossible';
                badgeClass = 'bg-secondary text-white';
                displayText = 'Không thể đạt';
            } else if (neededScore <= 0.05) {
                status = 'achieved';
                badgeClass = 'bg-success text-white';
                displayText = '≥ 0 (Chắc chắn đạt)';
            } else {
                status = 'possible';
                badgeClass = letter === 'A' ? 'bg-success text-white' : (letter.startsWith('B') ? 'bg-primary text-white' : 'bg-warning text-dark');
                displayText = '≥ ' + rounded.toFixed(1);
            }

            results.push({
                letter: letter,
                minScore10: minScore10,
                neededScore: rounded,
                status: status,
                badgeClass: badgeClass,
                displayText: displayText
            });
        });

        return {
            targetComp: targetComp,
            targetWeight: targetWeight,
            knownWeightedSum: Math.round(knownWeightedSum * 100) / 100,
            results: results
        };
    }

    /**
     * Cập nhật bảng gợi ý điểm cần đạt ngay trong Modal chỉnh sửa môn học
     */
    function updateModalPredictionTable(prefix) {
        const formulaInput = document.getElementById(prefix + 'Formula');
        const container = document.getElementById(prefix + 'QuickPredictionBox');
        if (!formulaInput || !container) return;

        const formula = formulaInput.value.trim();
        if (!formula) {
            container.innerHTML = '<div class="text-muted small py-2"><i class="fa-solid fa-circle-info me-1"></i>Chưa có công thức tính điểm để dự báo.</div>';
            return;
        }

        const scores = {
            'BT': parseNum(document.getElementById(prefix + 'ScoreBt')?.value),
            'BV': parseNum(document.getElementById(prefix + 'ScoreBv')?.value),
            'CK': parseNum(document.getElementById(prefix + 'ScoreCk')?.value),
            'DA': parseNum(document.getElementById(prefix + 'ScoreDa')?.value),
            'GK': parseNum(document.getElementById(prefix + 'ScoreGk')?.value),
            'QT': parseNum(document.getElementById(prefix + 'ScoreQt')?.value),
            'TH': parseNum(document.getElementById(prefix + 'ScoreTh')?.value)
        };

        const prediction = solveRequiredComponentScore(formula, scores, 'CK');
        if (!prediction) {
            container.innerHTML = '<div class="text-muted small py-2"><i class="fa-solid fa-circle-info me-1"></i>Công thức không có thành phần cuối kỳ [CK].</div>';
            return;
        }

        let html = `
            <div class="card border border-primary-subtle bg-primary-subtle bg-opacity-10 p-2 mt-2">
                <div class="d-flex justify-content-between align-items-center mb-1">
                    <span class="small fw-bold text-primary">
                        <i class="fa-solid fa-bullseye me-1"></i>Dự đoán điểm thi Cuối kỳ [CK] cần đạt:
                    </span>
                    <span class="badge bg-light text-primary border small">Trọng số CK: ${Math.round(prediction.targetWeight * 100)}%</span>
                </div>
                <div class="d-flex flex-wrap gap-1">
        `;

        prediction.results.forEach(item => {
            const isClickable = item.status === 'possible' || item.status === 'achieved';
            const clickAttr = isClickable 
                ? `onclick="window.applyPredictedScore('${prefix}', '${prediction.targetComp}', ${item.neededScore <= 0 ? 5.0 : item.neededScore})" role="button"`
                : '';
            const titleAttr = isClickable ? 'title="Bấm để thử áp dụng điểm số này"' : 'title="Không thể đạt mốc điểm này"';

            html += `
                <div class="p-1 px-2 rounded border bg-white small text-center flex-fill ${isClickable ? 'hover-shadow cursor-pointer' : 'opacity-50'}"
                     style="${isClickable ? 'cursor: pointer; transition: all 0.2s;' : ''}"
                     ${clickAttr} ${titleAttr}>
                    <div class="fw-bold text-dark">${item.letter}</div>
                    <span class="badge ${item.badgeClass} d-block mt-1" style="font-size: 0.75rem;">
                        ${item.displayText}
                    </span>
                </div>
            `;
        });

        html += `
                </div>
                <div class="text-muted small mt-1" style="font-size: 0.75rem;">
                    <i class="fa-solid fa-lightbulb text-warning me-1"></i>Mẹo: <strong>Bấm trực tiếp</strong> vào một mốc điểm chữ ở trên để tự động điền điểm thi CK dự kiến!
                </div>
            </div>
        `;

        container.innerHTML = html;
    }

    /**
     * Bấm vào mốc điểm chữ để tự động điền vào ô điểm CK
     */
    window.applyPredictedScore = function (prefix, comp, scoreVal) {
        const inputId = prefix + 'Score' + comp.charAt(0).toUpperCase() + comp.slice(1).toLowerCase();
        const input = document.getElementById(inputId);
        if (input) {
            // Giới hạn giá trị từ 0 đến 10
            let finalVal = Math.max(0, Math.min(10, scoreVal));
            input.value = finalVal.toFixed(1);
            if (typeof window.calculateSimulator === 'function') {
                window.calculateSimulator(prefix);
            }
        }
    };


    /* ==========================================================================
       PHẦN 2: BỘ MÔ PHỎNG MỤC TIÊU GPA TOÀN KHÓA / HỌC KỲ (GPA SANDBOX)
       ========================================================================== */

    const simState = {
        completedCredits: 0,
        completedWeightedPoints: 0,
        initialCpa: 0.0,
        inProgressCourses: [],
        simulatedCourses: [],
        targetGpa: 3.20
    };

    /**
     * Khởi tạo Sandbox từ dữ liệu các môn học hiện có
     */
    function initGpaSimulator(rawCourses) {
        if (!rawCourses || !Array.isArray(rawCourses)) return;

        let compCredits = 0;
        let compPoints = 0;
        const inProgress = [];

        rawCourses.forEach(c => {
            const credits = parseFloat(c.credits) || 0;
            // Bỏ qua môn 0 tín chỉ (GDTC/GDQP)
            if (credits <= 0) return;

            const s4 = (c.score4 !== null && c.score4 !== undefined && c.score4 !== '') ? parseFloat(c.score4) : null;
            const letter = c.scoreLetter ? c.scoreLetter.trim() : '';

            // Môn đã có điểm tổng kết
            if (s4 !== null && !isNaN(s4) && letter !== '' && letter !== 'Đang học') {
                compCredits += credits;
                compPoints += credits * s4;
            } else {
                // Môn đang học hoặc chưa có điểm
                inProgress.push({
                    id: c.id,
                    name: c.courseName,
                    semester: c.semester,
                    credits: credits,
                    currentLetter: letter || '',
                    currentScore4: s4,
                    simLetter: (letter && letter !== 'Đang học') ? letter : 'B', // Mặc định giả định điểm B
                    simScore4: (s4 !== null && !isNaN(s4)) ? s4 : 3.0
                });
            }
        });

        simState.completedCredits = compCredits;
        simState.completedWeightedPoints = compPoints;
        simState.initialCpa = compCredits > 0 ? (compPoints / compCredits) : 0.0;
        simState.inProgressCourses = inProgress;
        simState.simulatedCourses = JSON.parse(JSON.stringify(inProgress));

        renderSimulatorUI();
        recalculateGpaSim();
    }

    /**
     * Tính toán lại CPA mô phỏng thời gian thực
     */
    function recalculateGpaSim() {
        let totalCredits = simState.completedCredits;
        let totalPoints = simState.completedWeightedPoints;
        let simCredits = 0;

        simState.simulatedCourses.forEach(c => {
            const cr = c.credits;
            const s4 = c.simScore4 !== null ? c.simScore4 : 0.0;
            totalCredits += cr;
            totalPoints += cr * s4;
            simCredits += cr;
        });

        const liveCpa = totalCredits > 0 ? (totalPoints / totalCredits) : 0.0;
        const liveCpaRounded = Math.round(liveCpa * 100) / 100;
        const targetGpa = simState.targetGpa || 3.20;
        const diff = liveCpaRounded - simState.initialCpa;

        // Cập nhật lên UI
        const liveCpaEl = document.getElementById('simLiveCpa');
        const diffEl = document.getElementById('simCpaDiff');
        const totalCredsEl = document.getElementById('simTotalCredits');
        const progressBar = document.getElementById('simProgressBar');
        const targetBadge = document.getElementById('simTargetBadge');

        if (liveCpaEl) liveCpaEl.innerText = liveCpaRounded.toFixed(2);
        if (totalCredsEl) totalCredsEl.innerText = totalCredits.toFixed(1) + ' TC';

        if (diffEl) {
            if (diff > 0.001) {
                diffEl.className = 'badge bg-success';
                diffEl.innerHTML = `<i class="fa-solid fa-arrow-trend-up me-1"></i>+${diff.toFixed(2)}`;
            } else if (diff < -0.001) {
                diffEl.className = 'badge bg-danger';
                diffEl.innerHTML = `<i class="fa-solid fa-arrow-trend-down me-1"></i>${diff.toFixed(2)}`;
            } else {
                diffEl.className = 'badge bg-secondary';
                diffEl.innerHTML = `Giữ nguyên`;
            }
        }

        // Cập nhật thanh tiến độ đạt mục tiêu (Target Progress)
        if (progressBar) {
            const percent = Math.min(100, Math.max(0, (liveCpaRounded / 4.0) * 100));
            progressBar.style.width = percent + '%';
            if (liveCpaRounded >= targetGpa) {
                progressBar.className = 'progress-bar bg-success progress-bar-striped progress-bar-animated';
            } else if (liveCpaRounded >= targetGpa - 0.2) {
                progressBar.className = 'progress-bar bg-primary';
            } else {
                progressBar.className = 'progress-bar bg-warning';
            }
        }

        if (targetBadge) {
            if (liveCpaRounded >= targetGpa) {
                targetBadge.className = 'badge bg-success p-2 fs-6';
                targetBadge.innerHTML = `<i class="fa-solid fa-circle-check me-1"></i>Đạt mục tiêu (≥ ${targetGpa.toFixed(2)})`;
            } else {
                const neededDiff = targetGpa - liveCpaRounded;
                targetBadge.className = 'badge bg-secondary p-2 fs-6';
                targetBadge.innerHTML = `<i class="fa-solid fa-circle-xmark me-1"></i>Thiếu ${neededDiff.toFixed(2)} để đạt ${targetGpa.toFixed(2)}`;
            }
        }
    }

    /**
     * Thay đổi điểm chữ giả định của 1 môn học
     */
    window.updateCourseSimGrade = function (courseId, newLetter) {
        const item = simState.simulatedCourses.find(c => c.id === courseId);
        if (item) {
            item.simLetter = newLetter;
            item.simScore4 = letterToScore4(newLetter);
            recalculateGpaSim();
        }
    };

    /**
     * Render bảng danh sách các môn mô phỏng
     */
    function renderSimulatorUI() {
        const container = document.getElementById('simCoursesTableBody');
        const countBadge = document.getElementById('simCourseCountBadge');
        if (!container) return;

        if (countBadge) {
            countBadge.innerText = simState.simulatedCourses.length + ' môn';
        }

        if (simState.simulatedCourses.length === 0) {
            container.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center py-4 text-muted">
                        <i class="fa-solid fa-graduation-cap fa-2x mb-2 text-secondary"></i>
                        <p class="mb-0">Hiện tại tất cả các môn của bạn đều đã có điểm đầy đủ!</p>
                        <small class="text-secondary">Bạn có thể dùng nút "+ Thêm môn giả định" để thử nghiệm các kỳ tương lai.</small>
                    </td>
                </tr>
            `;
            return;
        }

        let html = '';
        simState.simulatedCourses.forEach((c, idx) => {
            html += `
                <tr id="simRow_${c.id}">
                    <td class="text-center text-muted">${idx + 1}</td>
                    <td>
                        <strong class="text-primary">${escapeHtml(c.name)}</strong>
                        <div class="small text-muted">Học kỳ: ${escapeHtml(c.semester || '-')}</div>
                    </td>
                    <td class="text-center fw-bold">${c.credits}</td>
                    <td class="text-center">
                        <select class="form-select form-select-sm fw-bold text-center border-primary mx-auto" 
                                style="max-width: 110px;"
                                onchange="window.updateCourseSimGrade(${c.id}, this.value)">
                            ${GRADE_CONVERSION.map(opt => `
                                <option value="${opt.letter}" ${opt.letter === c.simLetter ? 'selected' : ''}>
                                    ${opt.letter} (${opt.score4.toFixed(1)})
                                </option>
                            `).join('')}
                        </select>
                    </td>
                    <td class="text-center">
                        <span class="badge ${getLetterBadgeClass(c.simLetter)}">
                            ${c.simLetter}
                        </span>
                    </td>
                </tr>
            `;
        });

        container.innerHTML = html;
    }

    /**
     * Thuật toán Gợi ý kịch bản tối ưu (Smart Optimizer)
     */
    window.suggestOptimalScenarios = function () {
        const targetInput = document.getElementById('simTargetGpaInput');
        if (!targetInput) return;

        const targetGpa = parseFloat(targetInput.value);
        if (isNaN(targetGpa) || targetGpa < 1.0 || targetGpa > 4.0) {
            alert('Vui lòng nhập mục tiêu GPA hợp lệ từ 1.00 đến 4.00!');
            return;
        }

        simState.targetGpa = targetGpa;

        const inProg = simState.simulatedCourses;
        if (inProg.length === 0) {
            alert('Không có môn học nào đang học để gợi ý kịch bản!');
            return;
        }

        // Tính CPA tối đa có thể đạt được (khi tất cả các môn đều A = 4.0)
        let totalCredits = simState.completedCredits;
        let maxPoints = simState.completedWeightedPoints;
        let minPoints = simState.completedWeightedPoints;
        let inProgCredits = 0;

        inProg.forEach(c => {
            totalCredits += c.credits;
            maxPoints += c.credits * 4.0;
            minPoints += c.credits * 0.0;
            inProgCredits += c.credits;
        });

        const maxCpa = totalCredits > 0 ? (maxPoints / totalCredits) : 0.0;
        const minCpa = totalCredits > 0 ? (minPoints / totalCredits) : 0.0;

        const resultsContainer = document.getElementById('simScenarioResults');
        if (!resultsContainer) return;

        if (targetGpa > maxCpa + 0.001) {
            // Không khả thi trong kỳ này
            const neededTotalPoints = targetGpa * totalCredits;
            const extraPointsNeeded = neededTotalPoints - maxPoints;
            const extraACredits = Math.ceil((extraPointsNeeded / (4.0 - targetGpa)) * 2) / 2;

            resultsContainer.innerHTML = `
                <div class="alert alert-warning border-warning shadow-sm mt-3 mb-0">
                    <h6 class="fw-bold text-dark mb-1">
                        <i class="fa-solid fa-triangle-exclamation text-warning me-2"></i>Mục tiêu ${targetGpa.toFixed(2)} không thể đạt trọn vẹn trong kỳ này!
                    </h6>
                    <p class="small mb-2">
                        Dù tất cả <strong>${inProg.length} môn đang học</strong> (${inProgCredits} TC) đều đạt điểm <strong>A (4.0)</strong> thì CPA tối đa bạn có thể đạt là <strong class="text-primary">${maxCpa.toFixed(2)}</strong>.
                    </p>
                    <div class="small text-muted bg-white p-2 rounded border">
                        <i class="fa-solid fa-calculator me-1"></i>Để chạm mốc <strong>${targetGpa.toFixed(2)}</strong>, bạn cần tích lũy thêm khoảng <strong>${extraACredits > 0 ? extraACredits : 3} tín chỉ đạt A</strong> ở các kỳ kế tiếp.
                    </div>
                    <div class="mt-2 text-end">
                        <button class="btn btn-sm btn-outline-primary fw-semibold" onclick="window.applyScenario('max_effort')">
                            <i class="fa-solid fa-bolt me-1"></i>Thử kịch bản tối đa (Toàn A - ${maxCpa.toFixed(2)})
                        </button>
                    </div>
                </div>
            `;
            return;
        }

        // Kịch bản 1: Đều tay (An toàn)
        // Tìm điểm chữ đồng đều thấp nhất sao cho CPA >= targetGpa
        const candidateLetters = ['D', 'C', 'C+', 'B', 'B+', 'A'];
        let bestUniformLetter = 'A';
        for (let l of candidateLetters) {
            let pts = simState.completedWeightedPoints;
            let val4 = letterToScore4(l);
            inProg.forEach(c => pts += c.credits * val4);
            let testCpa = pts / totalCredits;
            if (testCpa >= targetGpa - 0.001) {
                bestUniformLetter = l;
                break;
            }
        }

        // Kịch bản 2: Trọng số tín chỉ (Ưu tiên môn nhiều tín chỉ điểm cao, giảm tải môn ít TC)
        // Sort môn theo tín chỉ giảm dần
        const sortedIndices = inProg.map((c, i) => ({ index: i, credits: c.credits }))
                                    .sort((a, b) => b.credits - a.credits);
        
        const creditWeightedPlan = {};
        // Ban đầu gán tất cả là B (3.0), sau đó nâng môn nhiều tín chỉ lên A/B+ nếu thiếu, hoặc hạ môn ít tín chỉ xuống C nếu thừa
        let curPts = simState.completedWeightedPoints;
        sortedIndices.forEach(item => {
            creditWeightedPlan[item.index] = 'B';
            curPts += item.credits * 3.0;
        });

        // Nếu chưa đạt target, nâng từ môn nhiều tín chỉ nhất lên A (4.0) hoặc B+ (3.5)
        if ((curPts / totalCredits) < targetGpa) {
            for (let item of sortedIndices) {
                // Thử nâng lên A
                curPts += item.credits * (4.0 - 3.0);
                creditWeightedPlan[item.index] = 'A';
                if ((curPts / totalCredits) >= targetGpa - 0.001) break;
            }
        } else {
            // Nếu đã vượt, hạ bớt môn ít tín chỉ nhất xuống C+ hoặc C để giảm áp lực
            const reverseSorted = [...sortedIndices].reverse();
            for (let item of reverseSorted) {
                let diff = item.credits * (3.0 - 2.5);
                if (((curPts - diff) / totalCredits) >= targetGpa - 0.001) {
                    curPts -= diff;
                    creditWeightedPlan[item.index] = 'C+';
                }
            }
        }

        window._cachedScenarios = {
            uniform: bestUniformLetter,
            creditWeighted: creditWeightedPlan,
            inProg: inProg
        };

        resultsContainer.innerHTML = `
            <div class="row g-2 mt-2">
                <div class="col-md-6">
                    <div class="card h-100 border-primary shadow-sm">
                        <div class="card-body p-3">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <h6 class="fw-bold text-primary mb-0">
                                    <i class="fa-solid fa-scale-balanced me-1"></i>Kịch bản 1: Đồng đều
                                </h6>
                                <span class="badge bg-primary">An toàn</span>
                            </div>
                            <p class="small text-muted mb-2">
                                Giữ phong độ đều đặn ở tất cả các môn: phấn đấu tối thiểu đạt điểm <strong>${bestUniformLetter}</strong> cho mọi môn đang học.
                            </p>
                            <button class="btn btn-sm btn-outline-primary w-100 fw-semibold" onclick="window.applyScenario('uniform')">
                                <i class="fa-solid fa-check me-1"></i>Áp dụng kịch bản Đồng đều
                            </button>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="card h-100 border-success shadow-sm">
                        <div class="card-body p-3">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <h6 class="fw-bold text-success mb-0">
                                    <i class="fa-solid fa-chart-pie me-1"></i>Kịch bản 2: Tối ưu tín chỉ
                                </h6>
                                <span class="badge bg-success">Chiến lược</span>
                            </div>
                            <p class="small text-muted mb-2">
                                Dồn sức lấy điểm <strong>A/B+</strong> cho các môn 3-4 tín chỉ; các môn nhẹ 1-2 tín chỉ chỉ cần mức điểm vừa phải để giảm tải.
                            </p>
                            <button class="btn btn-sm btn-outline-success w-100 fw-semibold" onclick="window.applyScenario('credit_weighted')">
                                <i class="fa-solid fa-check me-1"></i>Áp dụng kịch bản Chiến lược
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    };

    /**
     * Áp dụng kịch bản gợi ý vào danh sách môn
     */
    window.applyScenario = function (type) {
        if (!window._cachedScenarios && type !== 'max_effort') return;

        if (type === 'max_effort') {
            simState.simulatedCourses.forEach(c => {
                c.simLetter = 'A';
                c.simScore4 = 4.0;
            });
        } else if (type === 'uniform') {
            const letter = window._cachedScenarios.uniform;
            const val4 = letterToScore4(letter);
            simState.simulatedCourses.forEach(c => {
                c.simLetter = letter;
                c.simScore4 = val4;
            });
        } else if (type === 'credit_weighted') {
            const plan = window._cachedScenarios.creditWeighted;
            simState.simulatedCourses.forEach((c, idx) => {
                const letter = plan[idx] || 'B';
                c.simLetter = letter;
                c.simScore4 = letterToScore4(letter);
            });
        }

        renderSimulatorUI();
        recalculateGpaSim();
    };

    /**
     * Đặt lại tất cả điểm mô phỏng về trạng thái ban đầu
     */
    window.resetSimulation = function () {
        simState.simulatedCourses = JSON.parse(JSON.stringify(simState.inProgressCourses));
        renderSimulatorUI();
        recalculateGpaSim();
        const resultsContainer = document.getElementById('simScenarioResults');
        if (resultsContainer) resultsContainer.innerHTML = '';
    };

    /**
     * Bấm lưu kết quả mô phỏng vào CSDL thật
     */
    window.saveSimulationToDb = function () {
        if (simState.simulatedCourses.length === 0) {
            alert('Không có môn học mô phỏng nào để lưu!');
            return;
        }

        if (!confirm('Bạn có chắc chắn muốn lưu các điểm dự kiến này vào bảng điểm chính thức không? Các điểm này sẽ được cập nhật vào CSDL.')) {
            return;
        }

        const form = document.createElement('form');
        form.method = 'POST';
        form.action = 'grades';

        const actionInput = document.createElement('input');
        actionInput.type = 'hidden';
        actionInput.name = 'action';
        actionInput.value = 'apply_simulation';
        form.appendChild(actionInput);

        simState.simulatedCourses.forEach(c => {
            const idInput = document.createElement('input');
            idInput.type = 'hidden';
            idInput.name = 'simGradeIds';
            idInput.value = c.id;
            form.appendChild(idInput);

            const letterInput = document.createElement('input');
            letterInput.type = 'hidden';
            letterInput.name = 'simLetters';
            letterInput.value = c.simLetter;
            form.appendChild(letterInput);
        });

        document.body.appendChild(form);
        form.submit();
    };

    // Helper functions
    function parseNum(val) {
        if (val === undefined || val === null || val === '') return null;
        const n = parseFloat(val);
        return isNaN(n) ? null : n;
    }

    function escapeHtml(text) {
        if (!text) return '';
        return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }

    function getLetterBadgeClass(letter) {
        if (!letter) return 'bg-secondary';
        switch (letter.toUpperCase()) {
            case 'A': return 'bg-success';
            case 'B+':
            case 'B': return 'bg-primary';
            case 'C+':
            case 'C': return 'bg-warning text-dark';
            case 'D+':
            case 'D': return 'bg-secondary';
            default: return 'bg-danger';
        }
    }

    // Export module ra window để dashboard.jsp và dashboard.js sử dụng
    window.DutSimulator = {
        updateModalPredictionTable: updateModalPredictionTable,
        initGpaSimulator: initGpaSimulator,
        recalculateGpaSim: recalculateGpaSim,
        solveRequiredComponentScore: solveRequiredComponentScore
    };

})();
