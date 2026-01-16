package org.wikipedia.wikidata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.wikipedia.databinding.FragmentWikidataEditBinding
import org.wikipedia.dataclient.wikidata.Entities
import org.wikipedia.util.FeedbackUtil
import org.wikipedia.util.Resource

class WikidataEditFragment : Fragment() {

    private var _binding: FragmentWikidataEditBinding? = null
    val binding get() = _binding!!

    private val viewModel: WikidataEditViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWikidataEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.wikidataState.collect { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.contentLayout.visibility = View.GONE
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.contentLayout.visibility = View.VISIBLE
                            state.data?.let { entity ->
                                displayWikidataInfo(entity)
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            FeedbackUtil.showError(requireActivity(), state.throwable)
                        }
                    }
                }
            }
        }
    }

    private fun displayWikidataInfo(entity: Entities.Entity) {
        val languageCode = viewModel.pageTitle.wikiSite.languageCode
        
        // Display title (label)
        val label = entity.getLabels()[languageCode]?.value ?: ""
        binding.wikidataTitle.text = label
        
        // Display description
        val description = entity.getDescriptions()[languageCode]?.value ?: ""
        binding.wikidataDescription.text = description
        
        // Display aliases
        val aliases = entity.getAliases()[languageCode] ?: emptyList()
        if (aliases.isNotEmpty()) {
            binding.aliasesLabel.visibility = View.VISIBLE
            binding.aliasesList.visibility = View.VISIBLE
            binding.aliasesList.text = aliases.joinToString("\n") { "• ${it.value}" }
        } else {
            binding.aliasesLabel.visibility = View.GONE
            binding.aliasesList.visibility = View.GONE
        }
        
        // Display Wikidata ID
        binding.wikidataId.text = entity.id
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
