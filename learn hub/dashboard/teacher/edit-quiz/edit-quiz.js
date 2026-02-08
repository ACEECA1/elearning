// Fonction globale pour supprimer une question (appelée via onclick dans le HTML)
function deleteQuestion(id) {
    var question = $('.question-item[data-question="' + id + '"]');
    question.remove();
}

$(document).ready(function() {

    var sidebar = $('#sidebar');
    
    $('#mobileSidebarToggle').click(function() {
        if (sidebar.is(':visible')) {
            sidebar.hide();
        } else {
            sidebar.show();
        }
    });

    $('#notificationBtn').click(function() {
        var btn = $(this);
        btn.animate({ top: "-5px" }, "fast");
        btn.animate({ top: "5px" }, "fast");
        btn.animate({ top: "0px" }, "fast");
    });

    $('.search-bar input').keypress(function(e) {
        if (e.which == 13) {
            var texte = $(this).val();
            if (texte != "") {
                alert("Recherche : " + texte);
            }
        }
    });

    // Gestion de la fenêtre modale
    $('#openModalBtn').click(function() {
        $('#quizModal').fadeIn();
    });

    $('#closeModalBtn').click(function() {
        $('#quizModal').fadeOut();
    });

    // Sélection du type de quiz
    $('.quiz-type-card').click(function() {
        $('.quiz-type-card').removeClass('selected');
        $(this).addClass('selected');
    });

    // Ajout de questions dynamiquement
    var compteur = 1;

    $('#addQuestionBtn').click(function() {
        compteur = compteur + 1;

        // Construction du HTML pour la nouvelle question
        var html = '<div class="question-item" data-question="' + compteur + '">';
        html = html + '<div class="question-header">';
        html = html + '<span class="question-label">QUESTION ' + compteur + '</span>';
        html = html + '<button type="button" class="delete-question-btn" onclick="deleteQuestion(' + compteur + ')">';
        html = html + '<i class="fas fa-trash-alt"></i>';
        html = html + '</button>';
        html = html + '</div>';
        html = html + '<input type="text" class="question-input" placeholder="Enter question text...">';
        html = html + '<div class="options-list">';
        html = html + '<input type="text" class="option-input" placeholder="Option 1">';
        html = html + '<input type="text" class="option-input" placeholder="Option 2">';
        html = html + '</div>';
        html = html + '</div>';

        $('#questionsSection').append(html);
    });

    // Enregistrement du formulaire
    $('#quizEditorForm').submit(function(e) {
        e.preventDefault();
        
        var titre = $('#quizTitle').val();
        
        if (titre == "") {
            alert("Veuillez entrer un titre pour le quiz.");
        } else {
            alert("Le quiz '" + titre + "' a été enregistré.");
            $('#quizModal').fadeOut();
            $('#quizTitle').val("");
        }
    });

    // Interactions de la barre d'outils
    $('.toolbar-icon-btn').click(function() {
        $(this).css('opacity', '0.5');
        var btn = $(this);
        setTimeout(function() {
            btn.css('opacity', '1');
        }, 200);
    });

    $('.btn-publish').click(function() {
        alert("Quiz publié avec succès !");
    });

    $('.btn-share').click(function() {
        alert("Lien copié dans le presse-papier.");
    });

    $('.view-btn').click(function() {
        $('.view-btn').removeClass('active');
        $(this).addClass('active');
    });

    console.log("Éditeur de quiz chargé");
});