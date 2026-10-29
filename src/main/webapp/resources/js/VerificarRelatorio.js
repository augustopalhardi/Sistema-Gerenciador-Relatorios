/* ==========================================================
   Editor do documento formatado (tela Formatar Relatorio)

   - Historico proprio de desfazer/refazer (snapshots do HTML)
   - Fotos: selecao, arraste para reordenar, redimensionar,
     girar, cortar (canvas) e excluir
   - Formas SVG sobre as fotos (setas, circulos, retangulos)
   - Impressao: clona o documento e converte as posicoes das
     formas de px para % da foto, para acompanhar a escala
   ========================================================== */

/* ----------------------------------------------------------
   ESTADO GLOBAL
---------------------------------------------------------- */
var undoPilha = [];
var redoPilha = [];
var undoEmAndamento = false;

var fotoWrapperSelecionado = null;
var formaSelecionada = null;

var opAtiva = null;
var DRAG_THRESHOLD = 6;

/* ----------------------------------------------------------
   UNDO / REDO
---------------------------------------------------------- */
function gravarSnapshot() {
    if (undoEmAndamento) return;
    var area = document.getElementById('rtaEditorArea');
    if (!area) return;
    var snap = area.innerHTML;
    if (undoPilha.length > 0 && undoPilha[undoPilha.length - 1] === snap) return;
    undoPilha.push(snap);
    if (undoPilha.length > 60) undoPilha.shift();
    redoPilha = [];
    atualizarBotoesHistorico();
}

function acaoUndo() {
    if (undoPilha.length < 2) return;
    var area = document.getElementById('rtaEditorArea');
    if (!area) return;
    undoEmAndamento = true;
    redoPilha.push(undoPilha.pop());
    area.innerHTML = undoPilha[undoPilha.length - 1];
    reinicializarTudo();
    undoEmAndamento = false;
    atualizarBotoesHistorico();
}

function acaoRedo() {
    if (redoPilha.length === 0) return;
    var area = document.getElementById('rtaEditorArea');
    if (!area) return;
    undoEmAndamento = true;
    var snap = redoPilha.pop();
    undoPilha.push(snap);
    area.innerHTML = snap;
    reinicializarTudo();
    undoEmAndamento = false;
    atualizarBotoesHistorico();
}

function atualizarBotoesHistorico() {
    var u = document.getElementById('btnUndo');
    var r = document.getElementById('btnRedo');
    if (u) u.disabled = undoPilha.length < 2;
    if (r) r.disabled = redoPilha.length === 0;
}

/* ----------------------------------------------------------
   REINICIALIZACAO (apos undo/redo)
---------------------------------------------------------- */
function reinicializarTudo() {
    opAtiva = null;
    formaSelecionada = null;
    fotoWrapperSelecionado = null;
    limparDragState();

    var area = document.getElementById('rtaEditorArea');
    if (!area) return;

    var items = area.querySelectorAll('.rta-foto-item');
    for (var i = 0; i < items.length; i++) converterParaWrapper(items[i]);
}

/* ----------------------------------------------------------
   SINCRONIZAR CONTEUDO COM O BEAN
---------------------------------------------------------- */
function sincronizarConteudo() {
    var area  = document.getElementById('rtaEditorArea');
    var campo = document.getElementById('editorForm:campoConteudoEditor');
    if (area && campo) campo.value = area.innerHTML;
}

/* ----------------------------------------------------------
   SELECAO DE FOTO
---------------------------------------------------------- */
function selecionarFoto(wrapper) {
    if (fotoWrapperSelecionado && fotoWrapperSelecionado !== wrapper) {
        fotoWrapperSelecionado.classList.remove('foto-selecionada');
    }
    fotoWrapperSelecionado = wrapper;
    wrapper.classList.add('foto-selecionada');

    var todas = document.querySelectorAll('#rtaEditorArea .foto-wrapper');
    var idx = 0;
    for (var i = 0; i < todas.length; i++) {
        if (todas[i] === wrapper) { idx = i + 1; break; }
    }
    var lbl = document.getElementById('fotoAtivaLabel');
    if (lbl) lbl.textContent = 'Foto ' + idx + ' selecionada';
}

function desselecionarFoto() {
    if (fotoWrapperSelecionado) {
        fotoWrapperSelecionado.classList.remove('foto-selecionada');
        fotoWrapperSelecionado = null;
    }
    var lbl = document.getElementById('fotoAtivaLabel');
    if (lbl) lbl.textContent = 'Clique em uma foto';
}

/* ----------------------------------------------------------
   INICIALIZACAO DE FOTOS
---------------------------------------------------------- */
function inicializarFotos() {
    setTimeout(function() {
        var area = document.getElementById('rtaEditorArea');
        if (!area) return;
        var items = area.querySelectorAll('.rta-foto-item');
        for (var i = 0; i < items.length; i++) converterParaWrapper(items[i]);
        setTimeout(gravarSnapshot, 300);
        atualizarBotoesHistorico();
    }, 250);
}

/* ----------------------------------------------------------
   CONVERTER rta-foto-item -> foto-wrapper
---------------------------------------------------------- */
function converterParaWrapper(item) {
    if (item.classList.contains('foto-wrapper')) return;
    var img = item.querySelector('img');
    var legenda = item.querySelector('.rta-foto-legenda');
    if (!img) return;

    var wrapper = document.createElement('div');
    wrapper.className = 'foto-wrapper';
    var larg = img.style.width || '220px';
    if (String(larg).indexOf('px') === -1 && String(larg).indexOf('%') === -1) larg += 'px';
    wrapper.style.width = larg;

    img.style.width = '100%';
    img.style.height = 'auto';
    img.style.maxHeight = 'none';
    img.className = 'rta-foto';

    var badge = document.createElement('div');
    badge.className = 'foto-resize-badge';
    wrapper.appendChild(badge);

    wrapper.appendChild(img);
    if (legenda) { legenda.className = 'rta-foto-legenda'; wrapper.appendChild(legenda); }

    var handle = document.createElement('div');
    handle.className = 'foto-resize-handle';
    wrapper.appendChild(handle);

    var rotHandle = document.createElement('div');
    rotHandle.className = 'foto-rotate-handle';
    wrapper.appendChild(rotHandle);

    var rotBadge = document.createElement('div');
    rotBadge.className = 'foto-rotate-badge';
    wrapper.appendChild(rotBadge);

    item.parentNode.insertBefore(wrapper, item);
    item.parentNode.removeChild(item);
}

/* ----------------------------------------------------------
   MOVER FOTO POR BOTAO
---------------------------------------------------------- */
function moverFoto(wrapper, direcao) {
    var grid = wrapper.closest('.rta-fotos-grid');
    if (!grid) return;

    var wrappers = Array.prototype.slice.call(grid.querySelectorAll(':scope > .foto-wrapper'));
    var idx = wrappers.indexOf(wrapper);
    if (idx === -1) return;

    if (direcao === 'esq' && idx > 0) {
        grid.insertBefore(wrapper, wrappers[idx - 1]);
    } else if (direcao === 'dir' && idx < wrappers.length - 1) {
        var proximo = wrappers[idx + 1];
        var aposProximo = proximo.nextElementSibling;
        if (aposProximo) {
            grid.insertBefore(wrapper, aposProximo);
        } else {
            grid.appendChild(wrapper);
        }
    }
    selecionarFoto(wrapper);
    gravarSnapshot();
}

/* ----------------------------------------------------------
   DRAG CUSTOMIZADO - ghost + indicador
---------------------------------------------------------- */
var dragGhost = null;
var dragIndicator = null;
var dragDropTarget = null;

function criarGhost(wrapper) {
    var ghost = document.createElement('div');
    ghost.className = 'foto-drag-ghost';
    var img = wrapper.querySelector('img.rta-foto');
    if (img) {
        var cloneImg = document.createElement('img');
        cloneImg.src = img.src;
        cloneImg.style.width = Math.min(wrapper.offsetWidth, 150) + 'px';
        ghost.appendChild(cloneImg);
    }
    document.body.appendChild(ghost);
    return ghost;
}

function criarIndicator() {
    var ind = document.createElement('div');
    ind.className = 'foto-drop-indicator';
    return ind;
}

function removerGhost() {
    if (dragGhost && dragGhost.parentNode) dragGhost.parentNode.removeChild(dragGhost);
    dragGhost = null;
}

function removerIndicator() {
    if (dragIndicator && dragIndicator.parentNode) dragIndicator.parentNode.removeChild(dragIndicator);
    dragIndicator = null;
}

function limparDragState() {
    removerGhost();
    removerIndicator();
    dragDropTarget = null;
    var els = document.querySelectorAll('.foto-arrastando,.foto-drop-target');
    for (var i = 0; i < els.length; i++) els[i].classList.remove('foto-arrastando', 'foto-drop-target');
}

function atualizarDropTarget(e, dragWrapper) {
    var area = document.getElementById('rtaEditorArea');
    if (!area) return;

    var grids = area.querySelectorAll('.rta-fotos-grid');
    var melhorGrid = null, melhorPos = null, melhorEl = null, menorDist = Infinity;

    // Tentar encontrar posição dentro de algum grid
    for (var g = 0; g < grids.length; g++) {
        var grid = grids[g];
        var gridRect = grid.getBoundingClientRect();
        if (e.clientY < gridRect.top - 50 || e.clientY > gridRect.bottom + 50) continue;

        var fotos = grid.querySelectorAll(':scope > .foto-wrapper');
        for (var f = 0; f < fotos.length; f++) {
            var foto = fotos[f];
            if (foto === dragWrapper) continue;
            var rect = foto.getBoundingClientRect();
            var centerX = rect.left + rect.width / 2;
            var centerY = rect.top + rect.height / 2;
            var dist = Math.sqrt(Math.pow(e.clientX - centerX, 2) + Math.pow(e.clientY - centerY, 2));
            if (dist < menorDist) {
                menorDist = dist;
                melhorGrid = grid;
                melhorEl = foto;
                melhorPos = e.clientX < centerX ? 'before' : 'after';
            }
        }
        if (!melhorEl) melhorGrid = grid;
    }

    removerIndicator();

    if (melhorEl) {
        dragIndicator = criarIndicator();
        dragIndicator.style.minHeight = melhorEl.offsetHeight + 'px';
        if (melhorPos === 'before') {
            melhorGrid.insertBefore(dragIndicator, melhorEl);
        } else {
            var next = melhorEl.nextSibling;
            if (next) melhorGrid.insertBefore(dragIndicator, next);
            else melhorGrid.appendChild(dragIndicator);
        }
        dragDropTarget = {
            grid: melhorGrid,
            beforeElement: melhorPos === 'before' ? melhorEl : (melhorEl.nextElementSibling || null)
        };
    } else if (melhorGrid) {
        dragIndicator = criarIndicator();
        melhorGrid.appendChild(dragIndicator);
        dragDropTarget = { grid: melhorGrid, beforeElement: null };
    } else {
        // Se não está perto de nenhum grid, permitir soltar como elemento solto
        // Encontrar o elemento mais proximo do cursor dentro do editor
        var children = area.children;
        var melhorChild = null, melhorChildDist = Infinity, melhorChildPos = null;
        for (var c = 0; c < children.length; c++) {
            var child = children[c];
            if (child === dragWrapper || child.classList.contains('foto-drop-indicator')) continue;
            var rect = child.getBoundingClientRect();
            var centerY = rect.top + rect.height / 2;
            var dist = Math.abs(e.clientY - centerY);
            if (dist < melhorChildDist) {
                melhorChildDist = dist;
                melhorChild = child;
                melhorChildPos = e.clientY < centerY ? 'before' : 'after';
            }
        }
        if (melhorChild) {
            dragIndicator = criarIndicator();
            dragIndicator.style.minHeight = '4px';
            dragIndicator.style.width = '100%';
            if (melhorChildPos === 'before') {
                area.insertBefore(dragIndicator, melhorChild);
            } else {
                var next = melhorChild.nextSibling;
                if (next) area.insertBefore(dragIndicator, next);
                else area.appendChild(dragIndicator);
            }
            dragDropTarget = { grid: area, beforeElement: melhorChildPos === 'before' ? melhorChild : (melhorChild.nextElementSibling || null) };
        } else {
            dragDropTarget = null;
        }
    }
}

function executarDrop(dragWrapper) {
    if (!dragDropTarget) return;
    var grid = dragDropTarget.grid;
    var before = dragDropTarget.beforeElement;
    if (before) grid.insertBefore(dragWrapper, before);
    else grid.appendChild(dragWrapper);
}

/* ==========================================================
   HANDLER GLOBAL: MOUSEDOWN / MOUSEMOVE / MOUSEUP
========================================================== */

document.addEventListener('mousedown', function(e) {
    var target = e.target;

    // Foto resize handle
    if (target.classList.contains('foto-resize-handle')) {
        e.preventDefault(); e.stopPropagation();
        var wrapper = target.closest('.foto-wrapper');
        if (!wrapper) return;
        var badge = wrapper.querySelector('.foto-resize-badge');
        var area = document.getElementById('rtaEditorArea');
        if (area) area.setAttribute('contenteditable', 'false');
        if (badge) badge.style.display = 'block';
        opAtiva = { tipo: 'foto-resize', wrapper: wrapper, badge: badge, startX: e.clientX, startW: wrapper.offsetWidth };
        return;
    }

    // Foto rotate handle
    if (target.classList.contains('foto-rotate-handle')) {
        e.preventDefault(); e.stopPropagation();
        var wrapper = target.closest('.foto-wrapper');
        if (!wrapper) return;
        var rotBadge = wrapper.querySelector('.foto-rotate-badge');
        var area = document.getElementById('rtaEditorArea');
        if (area) area.setAttribute('contenteditable', 'false');
        target.style.cursor = 'grabbing';
        if (rotBadge) rotBadge.style.display = 'block';
        opAtiva = { tipo: 'foto-rotate', wrapper: wrapper, handle: target, badge: rotBadge };
        return;
    }

    // Forma resize handle
    if (target.classList.contains('forma-resize-handle')) {
        e.preventDefault(); e.stopPropagation();
        var container = target.closest('.forma-wrapper');
        if (!container) return;
        opAtiva = { tipo: 'forma-resize', container: container, startX: e.clientX, startY: e.clientY, startW: container.offsetWidth, startH: container.offsetHeight };
        return;
    }

    // Forma rotate handle
    if (target.classList.contains('forma-rotate-handle')) {
        e.preventDefault(); e.stopPropagation();
        var container = target.closest('.forma-wrapper');
        if (!container) return;
        target.style.cursor = 'grabbing';
        opAtiva = { tipo: 'forma-rotate', container: container, handle: target };
        return;
    }

    // Forma mover
    var formaW = target.closest ? target.closest('.forma-wrapper') : null;
    if (formaW && !target.classList.contains('forma-resize-handle') && !target.classList.contains('forma-rotate-handle')) {
        e.preventDefault(); e.stopPropagation();
        selecionarForma(formaW);
        var fotoParent = formaW.closest('.foto-wrapper');
        opAtiva = { tipo: 'forma-move', container: formaW, fotoWrapper: fotoParent, startX: e.clientX, startY: e.clientY, startL: formaW.offsetLeft, startT: formaW.offsetTop };
        return;
    }

    // Foto wrapper: selecionar + potencial drag
    var fotoW = target.closest ? target.closest('.foto-wrapper') : null;
    if (fotoW) {
        e.preventDefault();
        selecionarFoto(fotoW);
        opAtiva = { tipo: 'foto-drag-pending', wrapper: fotoW, startX: e.clientX, startY: e.clientY, started: false };
        return;
    }

    // Clique fora
    if (target.closest && !target.closest('.vr-toolbar') && !target.closest('.forma-wrapper') && !target.closest('.foto-wrapper')) {
        desselecionarFoto();
    }
    if (formaSelecionada && target.closest && !target.closest('.forma-wrapper') && !target.closest('.vr-toolbar')) {
        formaSelecionada.classList.remove('selecionada');
        formaSelecionada = null;
    }
});

document.addEventListener('mousemove', function(e) {
    if (!opAtiva) return;
    var op = opAtiva;

    if (op.tipo === 'foto-drag-pending') {
        if (Math.abs(e.clientX - op.startX) + Math.abs(e.clientY - op.startY) > DRAG_THRESHOLD) {
            op.tipo = 'foto-drag'; op.started = true;
            op.wrapper.classList.add('foto-arrastando');
            dragGhost = criarGhost(op.wrapper);
            var area = document.getElementById('rtaEditorArea');
            if (area) area.setAttribute('contenteditable', 'false');
        } else return;
    }

    if (op.tipo === 'foto-drag') {
        if (dragGhost) { dragGhost.style.left = (e.clientX + 12) + 'px'; dragGhost.style.top = (e.clientY + 12) + 'px'; }
        atualizarDropTarget(e, op.wrapper);
        return;
    }

    if (op.tipo === 'foto-resize') {
        op.wrapper.style.width = Math.max(60, op.startW + (e.clientX - op.startX)) + 'px';
        if (op.badge) op.badge.textContent = op.wrapper.offsetWidth + ' x ' + Math.round(op.wrapper.offsetHeight) + ' px';
        return;
    }

    if (op.tipo === 'foto-rotate') {
        var rect = op.wrapper.getBoundingClientRect();
        var ang = Math.round(Math.atan2(e.clientY - (rect.top + rect.height/2), e.clientX - (rect.left + rect.width/2)) * (180/Math.PI)) + 90;
        op.wrapper.style.transform = 'rotate(' + ang + 'deg)';
        if (op.badge) op.badge.textContent = ang + 'deg';
        return;
    }

    if (op.tipo === 'forma-move' && op.fotoWrapper) {
        op.container.style.left = Math.max(0, Math.min(op.fotoWrapper.offsetWidth - op.container.offsetWidth, op.startL + (e.clientX - op.startX))) + 'px';
        op.container.style.top  = Math.max(0, Math.min(op.fotoWrapper.offsetHeight - op.container.offsetHeight, op.startT + (e.clientY - op.startY))) + 'px';
        return;
    }

    if (op.tipo === 'forma-resize') {
        op.container.style.width = Math.max(20, op.startW + (e.clientX - op.startX)) + 'px';
        op.container.style.height = Math.max(20, op.startH + (e.clientY - op.startY)) + 'px';
        return;
    }

    if (op.tipo === 'forma-rotate') {
        var rect = op.container.getBoundingClientRect();
        var ang = Math.round(Math.atan2(e.clientY - (rect.top + rect.height/2), e.clientX - (rect.left + rect.width/2)) * (180/Math.PI)) + 90;
        op.container.style.transform = 'rotate(' + ang + 'deg)';
        return;
    }
});

document.addEventListener('mouseup', function() {
    if (!opAtiva) return;
    var op = opAtiva;
    opAtiva = null;

    if (op.tipo === 'foto-drag') {
        if (op.started) {
            executarDrop(op.wrapper);
            op.wrapper.classList.remove('foto-arrastando');
            limparDragState();
            var area = document.getElementById('rtaEditorArea');
            if (area) area.setAttribute('contenteditable', 'true');
            gravarSnapshot();
        }
        return;
    }
    if (op.tipo === 'foto-drag-pending') return;

    if (op.tipo === 'foto-resize') {
        if (op.badge) op.badge.style.display = 'none';
        var area = document.getElementById('rtaEditorArea');
        if (area) area.setAttribute('contenteditable', 'true');
        gravarSnapshot(); return;
    }
    if (op.tipo === 'foto-rotate') {
        op.handle.style.cursor = 'grab';
        if (op.badge) op.badge.style.display = 'none';
        var area = document.getElementById('rtaEditorArea');
        if (area) area.setAttribute('contenteditable', 'true');
        gravarSnapshot(); return;
    }
    if (op.tipo === 'forma-move' || op.tipo === 'forma-resize') { gravarSnapshot(); return; }
    if (op.tipo === 'forma-rotate') { op.handle.style.cursor = 'grab'; gravarSnapshot(); return; }
});

/* ----------------------------------------------------------
   TECLADO + AUTO-SNAPSHOT
---------------------------------------------------------- */
document.addEventListener('keydown', function(e) {
    if (e.ctrlKey && e.key === 'z') { e.preventDefault(); acaoUndo(); }
    if (e.ctrlKey && (e.key === 'y' || e.key === 'Y')) { e.preventDefault(); acaoRedo(); }
});

var textoTimer = null;
document.addEventListener('input', function(e) {
    var area = document.getElementById('rtaEditorArea');
    if (!area || !area.contains(e.target)) return;
    clearTimeout(textoTimer);
    textoTimer = setTimeout(gravarSnapshot, 800);
});

/* ----------------------------------------------------------
   DOUBLE CLICK em forma = mudar cor
---------------------------------------------------------- */
document.addEventListener('dblclick', function(e) {
    var formaW = e.target.closest ? e.target.closest('.forma-wrapper') : null;
    if (!formaW) return;
    e.stopPropagation();
    var svg = formaW.querySelector('svg');
    if (!svg) return;
    var tipo = 'quadrado';
    if (svg.querySelector('circle')) tipo = 'circulo';
    else if (svg.querySelector('ellipse')) tipo = 'oval';
    else if (svg.querySelector('polygon')) tipo = 'seta-dir';
    var novaCor = prompt('Cor da forma (ex: #ff0000 ou orange):', '#d97706');
    if (novaCor) { svg.innerHTML = svgFormas[tipo](novaCor); gravarSnapshot(); }
});

/* ----------------------------------------------------------
   FORMAS SVG
---------------------------------------------------------- */
var svgFormas = {
    'quadrado': function(cor) { return '<rect x="2" y="2" width="96" height="96" fill="none" stroke="' + cor + '" stroke-width="4" rx="2"/>'; },
    'circulo':  function(cor) { return '<circle cx="50" cy="50" r="46" fill="none" stroke="' + cor + '" stroke-width="4"/>'; },
    'oval':     function(cor) { return '<ellipse cx="50" cy="50" rx="48" ry="28" fill="none" stroke="' + cor + '" stroke-width="4"/>'; },
    'seta-dir': function(cor) { return '<polygon points="10,42 68,42 68,28 95,50 68,72 68,58 10,58" fill="' + cor + '"/>'; }
};

function inserirForma(tipo) {
    if (!fotoWrapperSelecionado || !document.getElementById('rtaEditorArea').contains(fotoWrapperSelecionado)) {
        var todas = document.querySelectorAll('#rtaEditorArea .foto-wrapper');
        if (todas.length > 0) selecionarFoto(todas[0]);
        else { alert('Nenhuma foto disponível.'); return; }
    }
    var alvo = fotoWrapperSelecionado;
    var cor = document.getElementById('corForma').value || '#d97706';
    var fw = alvo.offsetWidth, fh = alvo.offsetHeight;
    var largForma = Math.max(40, Math.round(fw * 0.4));

    var container = document.createElement('div');
    container.className = 'forma-wrapper';
    container.style.width = largForma + 'px';
    container.style.height = largForma + 'px';
    container.style.left = Math.round((fw - largForma) / 2) + 'px';
    container.style.top = Math.round((fh - largForma) / 2) + 'px';

    var svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    svg.setAttribute('viewBox', '0 0 100 100');
    svg.innerHTML = svgFormas[tipo](cor);
    container.appendChild(svg);

    var rH = document.createElement('div'); rH.className = 'forma-resize-handle'; container.appendChild(rH);
    var rotH = document.createElement('div'); rotH.className = 'forma-rotate-handle'; container.appendChild(rotH);

    alvo.appendChild(container);
    selecionarForma(container);
    gravarSnapshot();
}

function selecionarForma(c) {
    if (formaSelecionada && formaSelecionada !== c) formaSelecionada.classList.remove('selecionada');
    formaSelecionada = c; c.classList.add('selecionada');
}

function removerFormaSelecionada() {
    if (formaSelecionada && formaSelecionada.parentNode) {
        formaSelecionada.parentNode.removeChild(formaSelecionada);
        formaSelecionada = null; gravarSnapshot();
    }
}

/* ----------------------------------------------------------
   GIRAR FOTO 90 GRAUS PARA A DIREITA
---------------------------------------------------------- */
function girarFoto90() {
    if (!fotoWrapperSelecionado) {
        alert('Selecione uma foto primeiro.');
        return;
    }
    var img = fotoWrapperSelecionado.querySelector('img.rta-foto');
    if (!img) return;

    gravarSnapshot();

    var canvas = document.createElement('canvas');
    var ctx = canvas.getContext('2d');

    var tempImg = new Image();
    tempImg.onload = function() {
        canvas.width = tempImg.naturalHeight;
        canvas.height = tempImg.naturalWidth;

        ctx.translate(canvas.width / 2, canvas.height / 2);
        ctx.rotate(90 * Math.PI / 180);
        ctx.drawImage(tempImg, -tempImg.naturalWidth / 2, -tempImg.naturalHeight / 2);

        img.src = canvas.toDataURL('image/png');

        fotoWrapperSelecionado.style.transform = '';

        img.style.width = '100%';
        img.style.height = 'auto';

        setTimeout(gravarSnapshot, 200);
    };
    tempImg.src = img.src;
}

/* ----------------------------------------------------------
   EXCLUIR FOTO SELECIONADA
---------------------------------------------------------- */
function excluirFotoSelecionada() {
    if (!fotoWrapperSelecionado) {
        alert('Selecione uma foto primeiro.');
        return;
    }
    if (!confirm('Deseja excluir esta foto?')) return;

    gravarSnapshot();

    var parent = fotoWrapperSelecionado.parentNode;
    parent.removeChild(fotoWrapperSelecionado);
    fotoWrapperSelecionado = null;

    var lbl = document.getElementById('fotoAtivaLabel');
    if (lbl) lbl.textContent = 'Clique em uma foto';

    setTimeout(gravarSnapshot, 300);
}

/* ----------------------------------------------------------
   COLUNAS DO GRID
---------------------------------------------------------- */
function alterarColunasGrid(num) {
    var area = document.getElementById('rtaEditorArea');
    if (!area) return;
    var grids = area.querySelectorAll('.rta-fotos-grid');
    for (var i = 0; i < grids.length; i++) grids[i].style.gridTemplateColumns = 'repeat(' + num + ',1fr)';
    gravarSnapshot();
}

/* ----------------------------------------------------------
   FERRAMENTAS DE TEXTO
---------------------------------------------------------- */
function execCmd(cmd) { document.getElementById('rtaEditorArea').focus(); document.execCommand(cmd, false, null); }
function execFontMaior() {
    document.getElementById('rtaEditorArea').focus();
    document.execCommand('fontSize', false, '7');
    var s = document.getElementById('rtaEditorArea').querySelectorAll('font[size="7"]');
    for (var i = 0; i < s.length; i++) { s[i].removeAttribute('size'); s[i].style.fontSize = '1.2em'; }
}
function execFontMenor() {
    document.getElementById('rtaEditorArea').focus();
    document.execCommand('fontSize', false, '7');
    var s = document.getElementById('rtaEditorArea').querySelectorAll('font[size="7"]');
    for (var i = 0; i < s.length; i++) { s[i].removeAttribute('size'); s[i].style.fontSize = '0.85em'; }
}
function abrirCorTexto() { document.getElementById('colorPicker').click(); }
function aplicarCor(cor) { document.getElementById('rtaEditorArea').focus(); document.execCommand('foreColor', false, cor); }

/* ----------------------------------------------------------
   CROP DE FOTOS
---------------------------------------------------------- */
var cropWrapperAtual = null, cropImgAtual = null;
var cropX = 20, cropY = 20, cropW = 200, cropH = 150;
var cropArrastando = false, cropResizando = false, cropHandleAtivo = '';
var cropStartX, cropStartY, cropStartCropX, cropStartCropY, cropStartCropW, cropStartCropH;

function abrirCrop(img, wrapper) {
    cropWrapperAtual = wrapper; cropImgAtual = img;
    var imgEl = document.getElementById('cropImgOriginal');
    imgEl.src = img.src;
    imgEl.onload = function() {
        var iw = imgEl.offsetWidth, ih = imgEl.offsetHeight;
        cropW = Math.round(iw * 0.8); cropH = Math.round(ih * 0.8);
        cropX = Math.round((iw - cropW) / 2); cropY = Math.round((ih - cropH) / 2);
        atualizarSelecaoCrop();
    };
    document.getElementById('cropModal').style.display = 'flex';
    iniciarEventosCrop();
}
function fecharCrop() { document.getElementById('cropModal').style.display = 'none'; removerEventosCrop(); }
function atualizarSelecaoCrop() {
    var sel = document.getElementById('cropSelecao');
    sel.style.left = cropX + 'px'; sel.style.top = cropY + 'px';
    sel.style.width = cropW + 'px'; sel.style.height = cropH + 'px';
}
function iniciarEventosCrop() {
    var c = document.getElementById('cropContainer'), sel = document.getElementById('cropSelecao');
    var h = sel.querySelectorAll('.crop-handle');
    sel.addEventListener('mousedown', onCropMoverStart);
    for (var i = 0; i < h.length; i++) h[i].addEventListener('mousedown', onCropHandleStart);
    c.addEventListener('mousedown', onCropContainerStart);
    document.addEventListener('mousemove', onCropMouseMove);
    document.addEventListener('mouseup', onCropMouseUp);
}
function removerEventosCrop() {
    var c = document.getElementById('cropContainer'), sel = document.getElementById('cropSelecao');
    if (sel) sel.removeEventListener('mousedown', onCropMoverStart);
    if (c) c.removeEventListener('mousedown', onCropContainerStart);
    document.removeEventListener('mousemove', onCropMouseMove);
    document.removeEventListener('mouseup', onCropMouseUp);
    if (sel) { var h = sel.querySelectorAll('.crop-handle'); for (var i = 0; i < h.length; i++) h[i].removeEventListener('mousedown', onCropHandleStart); }
}
function onCropContainerStart(e) {
    if (e.target.classList.contains('crop-handle') || e.target === document.getElementById('cropSelecao')) return;
    var rect = document.getElementById('cropContainer').getBoundingClientRect();
    cropX = e.clientX - rect.left; cropY = e.clientY - rect.top;
    cropW = 4; cropH = 4; cropResizando = true; cropHandleAtivo = 'br';
    cropStartX = e.clientX; cropStartY = e.clientY;
    cropStartCropX = cropX; cropStartCropY = cropY; cropStartCropW = cropW; cropStartCropH = cropH;
    e.preventDefault(); e.stopPropagation();
}
function onCropMoverStart(e) {
    if (e.target.classList.contains('crop-handle')) return;
    cropArrastando = true; cropStartX = e.clientX; cropStartY = e.clientY;
    cropStartCropX = cropX; cropStartCropY = cropY;
    e.preventDefault(); e.stopPropagation();
}
function onCropHandleStart(e) {
    cropResizando = true;
    if (e.target.classList.contains('crop-h-tl')) cropHandleAtivo = 'tl';
    else if (e.target.classList.contains('crop-h-tr')) cropHandleAtivo = 'tr';
    else if (e.target.classList.contains('crop-h-bl')) cropHandleAtivo = 'bl';
    else cropHandleAtivo = 'br';
    cropStartX = e.clientX; cropStartY = e.clientY;
    cropStartCropX = cropX; cropStartCropY = cropY; cropStartCropW = cropW; cropStartCropH = cropH;
    e.preventDefault(); e.stopPropagation();
}
function onCropMouseMove(e) {
    var imgEl = document.getElementById('cropImgOriginal');
    if (!imgEl) return;
    var iw = imgEl.offsetWidth, ih = imgEl.offsetHeight;
    var dx = e.clientX - cropStartX, dy = e.clientY - cropStartY;
    if (cropArrastando) {
        cropX = Math.max(0, Math.min(iw - cropW, cropStartCropX + dx));
        cropY = Math.max(0, Math.min(ih - cropH, cropStartCropY + dy));
    } else if (cropResizando) {
        if (cropHandleAtivo === 'br') { cropW = Math.max(20, Math.min(iw - cropX, cropStartCropW + dx)); cropH = Math.max(20, Math.min(ih - cropY, cropStartCropH + dy)); }
        else if (cropHandleAtivo === 'tl') { var nx = Math.max(0, cropStartCropX + dx), ny = Math.max(0, cropStartCropY + dy); cropW = Math.max(20, cropStartCropW - (nx - cropStartCropX)); cropH = Math.max(20, cropStartCropH - (ny - cropStartCropY)); cropX = nx; cropY = ny; }
        else if (cropHandleAtivo === 'tr') { var ny2 = Math.max(0, cropStartCropY + dy); cropW = Math.max(20, Math.min(iw - cropX, cropStartCropW + dx)); cropH = Math.max(20, cropStartCropH - (ny2 - cropStartCropY)); cropY = ny2; }
        else { var nx2 = Math.max(0, cropStartCropX + dx); cropW = Math.max(20, cropStartCropW - (nx2 - cropStartCropX)); cropH = Math.max(20, Math.min(ih - cropY, cropStartCropH + dy)); cropX = nx2; }
    }
    atualizarSelecaoCrop();
}
function onCropMouseUp() { cropArrastando = false; cropResizando = false; cropHandleAtivo = ''; }
function aplicarCrop() {
    var imgEl = document.getElementById('cropImgOriginal');
    var scaleX = imgEl.naturalWidth / imgEl.offsetWidth, scaleY = imgEl.naturalHeight / imgEl.offsetHeight;
    var canvas = document.createElement('canvas');
    canvas.width = Math.round(cropW * scaleX); canvas.height = Math.round(cropH * scaleY);
    canvas.getContext('2d').drawImage(imgEl, Math.round(cropX * scaleX), Math.round(cropY * scaleY), canvas.width, canvas.height, 0, 0, canvas.width, canvas.height);
    cropImgAtual.src = canvas.toDataURL('image/jpeg', 0.92);
    fecharCrop(); gravarSnapshot();
}
function toggleModoCrop() {
    if (!fotoWrapperSelecionado) {
        alert('Selecione uma foto primeiro.');
        return;
    }
    var img = fotoWrapperSelecionado.querySelector('img.rta-foto');
    if (img) abrirCrop(img, fotoWrapperSelecionado);
}

/* ----------------------------------------------------------
   IMPRIMIR / PDF
---------------------------------------------------------- */
function imprimirDocumento() {
    var areaOriginal = document.getElementById('rtaEditorArea');
    var areaClone = areaOriginal.cloneNode(true);

    var remover = areaClone.querySelectorAll(
        '.foto-resize-handle,.foto-resize-badge,' +
        '.forma-resize-handle,.foto-rotate-handle,.foto-rotate-badge,.forma-rotate-handle,' +
        '.foto-move-controls,.foto-drop-indicator'
    );
    for (var i = 0; i < remover.length; i++) remover[i].parentNode.removeChild(remover[i]);

    var formasC = areaClone.querySelectorAll('.forma-wrapper');
    for (var i = 0; i < formasC.length; i++) formasC[i].classList.remove('selecionada');
    var fwC = areaClone.querySelectorAll('.foto-wrapper');
    for (var i = 0; i < fwC.length; i++) fwC[i].classList.remove('foto-selecionada');

    var fotosOrig = areaOriginal.querySelectorAll('.foto-wrapper');
    var fotosClone = areaClone.querySelectorAll('.foto-wrapper');
    for (var f = 0; f < fotosOrig.length; f++) {
        var fOrig = fotosOrig[f], fClone = fotosClone[f];
        var fw = fOrig.offsetWidth, fh = fOrig.offsetHeight;
        if (!fw || !fh) continue;
        var fFormasOrig = fOrig.querySelectorAll('.forma-wrapper');
        var fFormasClone = fClone.querySelectorAll('.forma-wrapper');
        for (var ff = 0; ff < fFormasOrig.length; ff++) {
            var fo = fFormasOrig[ff], fc = fFormasClone[ff];
            fc.style.left = (fo.offsetLeft / fw * 100).toFixed(3) + '%';
            fc.style.top = (fo.offsetTop / fh * 100).toFixed(3) + '%';
            fc.style.width = (fo.offsetWidth / fw * 100).toFixed(3) + '%';
            fc.style.height = (fo.offsetHeight / fh * 100).toFixed(3) + '%';
        }
    }

    var wrappers = areaClone.querySelectorAll('.foto-wrapper');
    for (var i = 0; i < wrappers.length; i++) {
        wrappers[i].className = 'rta-foto-item';
        wrappers[i].style.display = 'flex';
        wrappers[i].style.flexDirection = 'column';
        wrappers[i].style.alignItems = 'center';
        wrappers[i].style.position = 'relative';
        wrappers[i].removeAttribute('draggable');
    }

    var conteudo = areaClone.innerHTML;
    var css = '<style>'
        + '@page{margin:10mm 10mm 10mm 10mm;}'
        + 'body{margin:0;padding:1rem;background:#fff;font-family:Georgia,serif;}'
        + '.vr-editor-area{max-width:100%;font-family:Georgia,serif;font-size:11pt;line-height:1.65;color:#1a1a1a;}'
        + '.rta-foto-item{display:flex;flex-direction:column;align-items:center;position:relative !important;}'
        + '.forma-wrapper{position:absolute;box-sizing:border-box;}'
        + '.forma-wrapper svg{width:100%;height:100%;display:block;overflow:visible;}'
        + '.rta-capa{display:flex;flex-direction:column;min-height:260px;background:#fff;border-bottom:4px solid #1a202c;}'
        + '.rta-capa-header{display:flex;justify-content:space-between;align-items:flex-start;padding:2rem 2.5rem 1rem;}'
        + '.rta-logo-empresa{max-height:70px;}'
        + '.rta-capa-logo-cliente-box{display:flex;align-items:center;}'
        + '.rta-logo-cliente{max-height:70px;max-width:180px;object-fit:contain;}'
        + '.rta-capa-ano{font-size:5rem;font-weight:800;color:#e2e8f0;line-height:1;font-family:Arial,sans-serif;letter-spacing:-3px;}'
        + '.rta-capa-body{padding:1rem 2.5rem 2rem;background:#1a202c;flex:1;}'
        + '.rta-capa-data-line{color:#94a3b8;font-size:.85rem;}'
        + '.rta-capa-titulo-principal{font-size:1.1rem;font-weight:700;color:#fff;line-height:1.4;}'
        + '.rta-separador-pagina{page-break-after:always;height:0;}'
        + '.rta-identificacao{padding:2rem 2.5rem;border-top:3px solid #d97706;}'
        + '.rta-title-main{font-size:1.8rem;font-weight:700;color:#1a202c;font-family:Arial,sans-serif;margin-bottom:1.2rem;}'
        + '.rta-id-bloco{margin-bottom:.8rem;padding-bottom:.8rem;border-bottom:1px solid #e2e8f0;font-size:.93rem;}'
        + '.rta-id-bloco p{margin:.25rem 0;}'
        + '.rta-destaque-cliente{color:#d97706;font-weight:600;}'
        + '.rta-destaque-empresa{color:#1e40af;font-weight:600;}'
        + '.rta-contato-empresa{padding:.7rem 1rem;background:#f8fafc;border-left:3px solid #1a202c;font-size:.8rem;color:#475569;line-height:1.6;}'
        + '.rta-sumario{padding:1.8rem 2.5rem;}'
        + '.rta-section-header{font-family:Arial,sans-serif;font-size:.95rem;font-weight:700;letter-spacing:1px;color:#1a202c;margin-bottom:.8rem;padding-bottom:.3rem;border-bottom:2px solid #1a202c;}'
        + '.rta-sumario-tabela{width:100%;border-collapse:collapse;font-size:.86rem;}'
        + '.rta-sumario-tabela tr{border-bottom:1px dotted #d1d5db;}'
        + '.rta-sum-num{font-weight:700;color:#d97706;padding:.35rem .7rem .35rem 0;white-space:nowrap;vertical-align:top;}'
        + '.rta-secao{padding:1.6rem 2.5rem;border-top:1px solid #e2e8f0;}'
        + '.rta-secao-header-only{padding-bottom:.2rem;border-top:1px solid #e2e8f0;padding-top:1.6rem;padding-left:2.5rem;padding-right:2.5rem;}'
        + '.rta-secao-titulo{font-family:Arial,sans-serif;font-size:.9rem;font-weight:700;color:#1a202c;text-transform:uppercase;margin-bottom:.8rem;}'
        + '.rta-num{color:#d97706;font-weight:800;}'
        + '.rta-paragrafo{text-align:justify;margin-bottom:.8rem;font-size:.88rem;line-height:1.7;}'
        + '.rta-topico{margin:1rem 0;padding:1rem;border:1px solid #e2e8f0;border-left:3px solid #d97706;background:#fffdf5;}'
        + '.rta-topico-titulo{font-family:Arial,sans-serif;font-size:.86rem;font-weight:700;text-transform:uppercase;color:#1a202c;margin-bottom:.7rem;}'
        + '.rta-subtopico{margin:.8rem 0 0 1.2rem;padding:.8rem;border:1px solid #e5e7eb;border-left:2px solid #93c5fd;}'
        + '.rta-subtopico-titulo{font-family:Arial,sans-serif;font-size:.83rem;font-weight:600;color:#2d3748;margin-bottom:.6rem;}'
        + '.rta-conteudo-rico{font-size:.86rem;line-height:1.7;margin-bottom:.6rem;text-align:justify;}'
        + '.rta-fotos-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:.7rem;margin:.8rem 0;}'
        + '.rta-foto{width:100%;height:auto;border:1px solid #e2e8f0;border-radius:4px;display:block;}'
        + '.rta-foto-legenda{font-size:.7rem;font-weight:600;color:#475569;text-align:center;}'
        + '.rta-recomendacoes{list-style:none;padding:0;}'
        + '.rta-recomendacoes li{display:flex;align-items:flex-start;gap:.5rem;font-size:.86rem;line-height:1.6;padding:.3rem 0;border-bottom:1px solid #f3f4f6;}'
        + '.rta-recomendacoes li::before{content:"\\2713";color:#d97706;font-weight:700;}'
        + '.rta-assinatura{padding:1.6rem 2.5rem;border-top:1px solid #e2e8f0;}'
        + '.rta-assinatura-texto{font-size:.84rem;line-height:1.65;color:#374151;text-align:justify;margin-bottom:1.5rem;}'
        + '.rta-assinatura-box{width:260px;}'
        + '.rta-assinatura-linha{height:1px;background:#1a202c;margin-bottom:.4rem;}'
        + '.rta-assinatura-nome{font-weight:700;font-size:.86rem;}'
        + '.rta-assinatura-empresa{font-size:.78rem;color:#475569;}'
        + '.rta-rodape{background:#1a202c;color:#94a3b8;text-align:center;font-size:.7rem;padding:.6rem 2rem;}'
        + '@media print{.rta-separador-pagina{page-break-after:always;}}'
        + '</style>';

    var janela = window.open('', '_blank', 'width=960,height=760');
    if (!janela) {
        alert('O navegador bloqueou a janela de impressão. Permita pop-ups para este site e tente novamente.');
        return;
    }
    janela.document.write('<!DOCTYPE html><html><head><meta charset="UTF-8"/>');
    janela.document.write('<title>Relatório Técnico</title>');
    janela.document.write(css);
    janela.document.write('</head><body><div class="vr-editor-area">');
    janela.document.write(conteudo);
    janela.document.write('</div><script>window.onload=function(){window.print();};<\/script>');
    janela.document.write('</body></html>');
    janela.document.close();
}

/* ----------------------------------------------------------
   ABERTURA DE UM RASCUNHO JA SALVO
   (o editor ja vem renderizado; inicializa fotos e historico)
---------------------------------------------------------- */
document.addEventListener('DOMContentLoaded', function() {
    if (document.getElementById('rtaEditorArea')) inicializarFotos();
});
