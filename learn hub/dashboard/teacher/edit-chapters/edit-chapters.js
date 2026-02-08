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

    $('#openModalBtn').click(function() {
        $('#quizModal').fadeIn();
    });

    $('#closeModalBtn').click(function() {
        $('#quizModal').fadeOut();
    });

    $('.quiz-type-card').click(function() {
        $('.quiz-type-card').removeClass('selected');
        $(this).addClass('selected');
    });

    var compteur = 1;

    $('#addQuestionBtn').click(function() {
        compteur = compteur + 1;

        var html = '<div class="question-item" data-question="' + compteur + '">';
        html += '<div class="question-header">';
        html += '<span class="question-label">QUESTION ' + compteur + '</span>';
        html += '<button type="button" class="delete-question-btn" onclick="deleteQuestion(' + compteur + ')">';
        html += '<i class="fas fa-trash-alt"></i>';
        html += '</button></div>';
        html += '<input type="text" class="question-input" placeholder="Enter question text...">';
        html += '<div class="options-list">';
        html += '<input type="text" class="option-input" placeholder="Option 1">';
        html += '<input type="text" class="option-input" placeholder="Option 2">';
        html += '</div></div>';

        $('#questionsSection').append(html);
    });

    $('#quizEditorForm').submit(function(e) {
        e.preventDefault();
        
        var titre = $('#quizTitle').val();
        
        if (titre == "") {
            alert("Veuillez entrer un titre pour le quiz.");
        } else {
            alert("Le quiz '" + titre + "' a été créé et ajouté au chapitre.");
            $('#quizModal').fadeOut();
            $('#quizTitle').val("");
        }
    });

    $('.toolbar-icon-btn').click(function() {
        $(this).css('color', '#4a3bbf');
        var btn = $(this);
        setTimeout(function() {
            btn.css('color', '');
        }, 200);
    });

    $('.btn-publish').click(function() {
        alert("Modifications publiées avec succès !");
    });

    $('.btn-share').click(function() {
        alert("Lien de partage copié.");
    });

    $('.view-btn').click(function() {
        $('.view-btn').removeClass('active');
        $(this).addClass('active');
    });

    console.log("Éditeur de chapitre chargé");
});