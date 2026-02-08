// Fonction globale pour la suppression (car appelée via onclick dans le HTML)
function deleteQuestion(id) {
    var question = $('.question-item[data-question="' + id + '"]');
    question.remove();
}

$(document).ready(function() {

    // --- Gestion du Menu Mobile ---
    var sidebar = $('#sidebar');
    $('#mobileSidebarToggle').click(function() {
        if (sidebar.is(':visible')) {
            sidebar.hide();
        } else {
            sidebar.show();
        }
    });

    // --- Gestion de la modale ---
    $('#openModalBtn').click(function() {
        $('#quizModal').fadeIn();
    });

    $('#closeModalBtn').click(function() {
        $('#quizModal').fadeOut();
    });

    // --- Sélection du type de quiz ---
    $('.quiz-type-card').click(function() {
        $('.quiz-type-card').removeClass('selected');
        $(this).addClass('selected');
    });

    // --- Ajout de question ---
    var compteurQuestions = 1;

    $('#addQuestionBtn').click(function() {
        compteurQuestions = compteurQuestions + 1;

        var html = '';
        html += '<div class="question-item" data-question="' + compteurQuestions + '">';
        html += '   <div class="question-header">';
        html += '       <span class="question-label">QUESTION ' + compteurQuestions + '</span>';
        html += '       <button type="button" class="delete-question-btn" onclick="deleteQuestion(' + compteurQuestions + ')">';
        html += '           <i class="fas fa-trash-alt"></i>';
        html += '       </button>';
        html += '   </div>';
        html += '   <input type="text" class="question-input" placeholder="Enter question text...">';
        html += '   <div class="options-list">';
        html += '       <input type="text" class="option-input" placeholder="Option 1">';
        html += '       <input type="text" class="option-input" placeholder="Option 2">';
        html += '   </div>';
        html += '</div>';

        $('#questionsSection').append(html);
    });

    // --- Sauvegarde du quiz ---
    $('#quizEditorForm').submit(function(e) {
        e.preventDefault();
        
        var titre = $('#quizTitle').val();
        
        if (titre == "") {
            alert("Veuillez donner un titre au quiz.");
        } else {
            alert("Quiz '" + titre + "' enregistré avec succès !");
            $('#quizModal').fadeOut();
            // Réinitialiser le formulaire si besoin
            $('#quizTitle').val("");
        }
    });

    // --- Boutons de la barre d'outils ---
    $('.toolbar-icon-btn').click(function() {
        // Juste pour l'effet visuel
        $(this).animate({ opacity: 0.5 }, 100, function() {
            $(this).animate({ opacity: 1 }, 100);
        });
    });

    $('.btn-publish').click(function() {
        alert("Quiz publié !");
    });

    console.log("Éditeur de quiz chargé");
});